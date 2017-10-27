package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.model.ModelField;
import com.denimgroup.threadfix.framework.impl.model.ModelFieldSet;
import com.denimgroup.threadfix.framework.impl.struts.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.NamespaceAnnotation;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class ConventionPluginMapper implements ActionMapper {


    static final SanitizedLogger log = new SanitizedLogger(ConventionPluginMapper.class.getName());

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String parentNamespace) {

        List<StrutsEndpoint> endpoints = list();

        String[] actionSuffixes = getActionSuffixes(project);

        for (StrutsClass strutsClass : project.getClasses()) {

            String rootNamespacePath = buildNamespace(parentNamespace, strutsClass, project);

            ModelFieldSet fields = strutsClass.getProperties();
            List<String> fieldNames = list();
            for (ModelField field : fields) {
                fieldNames.add(field.getParameterKey());
            }

            for (StrutsMethod method : strutsClass.getMethods()) {
                String methodPath = method.getName();

                String endpointPath = PathUtil.combine(rootNamespacePath, formatCamelCaseToConvention(method.getName(), project));
                StrutsEndpoint newEndpoint = new StrutsEndpoint(strutsClass.getSourceFile(), endpointPath, list("GET"), fieldNames);
                endpoints.add(newEndpoint);

                if (methodPath.equalsIgnoreCase("index")) {
                    endpointPath = rootNamespacePath;
                    newEndpoint = new StrutsEndpoint(strutsClass.getSourceFile(), endpointPath, list("GET"), fieldNames);
                    endpoints.add(newEndpoint);
                }
            }
        }

        return endpoints;
    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        return list(StrutsKnownPlugins.CONVENTION);
    }

    private static String[] getActionSuffixes(StrutsProject project) {
        return project.getConfig().get("struts.convention.action.suffix", "Action").split(",");
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
