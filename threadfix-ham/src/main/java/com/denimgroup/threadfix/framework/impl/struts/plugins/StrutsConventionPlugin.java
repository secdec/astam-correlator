////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.struts.plugins;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.NamespaceAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ParentPackageAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultAnnotation;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class StrutsConventionPlugin implements StrutsPlugin {

    static final SanitizedLogger log = new SanitizedLogger(StrutsConventionPlugin.class.getName());

    @Override
    public void apply(StrutsProject project) {
        List<StrutsPackage> newPackages = list();

        String[] actionSuffixes = getActionSuffixes(project);

        //  Should limit this based on whether the class inherits the necessary types, but since
        //      there are many possible types for many possible plugins/extensions/etc. it's
        //      more reliable to keep this unrestricted
        for (StrutsClass strutsClass : project.getClasses()) {

            String rootNamespacePath = buildNamespace("/", strutsClass, project);

            String className = strutsClass.getName();
            boolean hasSuffix = false;
            for (String suffix : actionSuffixes) {
                if (className.endsWith(suffix)) {
                    hasSuffix = true;
                    break;
                }
            }
            if (!hasSuffix) {
                continue;
            }

            String cleanClassName = strutsClass.getCleanName(actionSuffixes);

            StrutsPackage newPackage = new StrutsPackage();
            newPackage.setName(cleanClassName);
            newPackage.setNamespace(rootNamespacePath);
            ParentPackageAnnotation parentPackage = strutsClass.getFirstAnnotation(ParentPackageAnnotation.class);
            if (parentPackage != null) {
                newPackage.setPkgExtends(parentPackage.getPackageName());
            }

            newPackage.setSourceClass(strutsClass);

            Set<ModelField> fields = strutsClass.getProperties();
            Map<String, String> parameters = map();
            for (ModelField field : fields) {
                parameters.put(field.getParameterKey(), field.getType());
            }

            for (StrutsMethod method : strutsClass.getMethods()) {
                String endpointPath = formatCamelCaseToConvention(method.getName(), project);
                StrutsAction action = new StrutsAction(endpointPath, method.getName(), strutsClass.getName(), strutsClass.getSourceFile());
                action.setParams(parameters);

                Collection<Annotation> annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.getClass() == ResultAnnotation.class) {
                        ResultAnnotation resultAnnotation = (ResultAnnotation)annotation;
                        StrutsResult result = new StrutsResult(resultAnnotation.getResultName(), resultAnnotation.getResultType(), resultAnnotation.getResultLocation());
                        action.addResult(result);
                    }
                }

                newPackage.addAction(action);
            }

            newPackages.add(newPackage);
        }

        project.addPackages(newPackages);
    }

    private static String[] getActionSuffixes(StrutsProject project) {
        return project.getConfig().get("struts.convention.action.suffix", "Action,Controller").split(",");
    }

    public static String buildNamespace(String parentNamespace, StrutsClass forClass, StrutsProject forProject) {
        String[] locators = forProject.getConfig().get("struts.convention.package.locators", "action,actions,struts,struts2").split(",");
        String fullPackageName = forClass.getPackage();
        String[] packageNameParts = fullPackageName.split("\\.");
        int packageBasePart = -1;
        for (int i = 0; i < packageNameParts.length; i++) {
            String current = packageNameParts[i];
            for (String locator : locators) {
                if (locator.equalsIgnoreCase(current)) {
                    packageBasePart = i;
                    break;
                }
            }
        }

        if (packageBasePart < 0) {
            log.debug("Unable to determine base Convention package name for Java package " + fullPackageName);
            return null;
        }

        StringBuilder remainingPathBuilder = new StringBuilder();
        for (int i = packageBasePart + 1; i < packageNameParts.length; i++) {
            if (remainingPathBuilder.length() > 0) {
                remainingPathBuilder.append("/");
            }
            remainingPathBuilder.append(packageNameParts[i]);
        }

        String rootNamespacePath = remainingPathBuilder.toString();

        rootNamespacePath = formatCamelCaseToConvention(rootNamespacePath, forProject);
        NamespaceAnnotation classNamespaceAnnotation = forClass.getFirstAnnotation(NamespaceAnnotation.class);

        if (classNamespaceAnnotation != null) {
            rootNamespacePath = classNamespaceAnnotation.getNamespacePath();
        } else {
            if (parentNamespace != null) {
                rootNamespacePath = PathUtil.combine(parentNamespace, rootNamespacePath);
            }
        }


        String[] actionSuffixes = getActionSuffixes(forProject);
        rootNamespacePath = PathUtil.combine(rootNamespacePath, formatCamelCaseToConvention(forClass.getCleanName(actionSuffixes), forProject));
        return rootNamespacePath;
    }

    public static String formatCamelCaseToConvention(String camelCaseString, StrutsProject forProject) {
        String separator = forProject.getConfig().get("struts.convention.action.name.separator", "-");
        StringBuilder builder = new StringBuilder();
        boolean lastWasLowerCase = false;
        for (int i = 0; i < camelCaseString.length(); i++) {
            char c = camelCaseString.charAt(i);

            boolean currentIsUpperCase = Character.isUpperCase(c);
            if (currentIsUpperCase) {
                c = Character.toLowerCase(c);
                if (lastWasLowerCase) {
                    builder.append(separator);
                }
            }

            lastWasLowerCase = !currentIsUpperCase;

            builder.append(c);
        }

        return builder.toString();
    }
}
