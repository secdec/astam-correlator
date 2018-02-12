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

package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.framework.impl.struts.annotationParsers.*;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ActionAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

public class StrutsClassParser {

    static SanitizedLogger LOG = new SanitizedLogger(StrutsClassParser.class.getName());

    ActionAnnotationParser actionParser = new ActionAnnotationParser();
    NamespaceAnnotationParser namespaceParser = new NamespaceAnnotationParser();
    ParentPackageAnnotationParser parentPackageParser = new ParentPackageAnnotationParser();
    ResultAnnotationParser resultParser = new ResultAnnotationParser();
    ResultPathAnnotationParser resultPathParser = new ResultPathAnnotationParser();
    StrutsClassSignatureParser classSigParser = new StrutsClassSignatureParser();


    StrutsClass resultClass;


    public StrutsClassParser(File file) {

        EventBasedTokenizerRunner.run(file, true, actionParser, namespaceParser, parentPackageParser, resultParser, resultPathParser, classSigParser);
        //EventBasedTokenizerRunner.run(file, true, actionParser);

        String className = classSigParser.getParsedClassName();

        if (className == null) {
            return;
        }

        resultClass = new StrutsClass(className, file.getAbsolutePath());
        resultClass.addAllMethods(classSigParser.getParsedMethods());
        resultClass.setProperties(collectParameters(classSigParser.getParsedMethods()));
        resultClass.setImportedPackages(classSigParser.getImports());
        resultClass.setPackage(classSigParser.getClassPackage());


        for (String baseType : classSigParser.getBaseTypes()) {
            resultClass.addBaseType(baseType);
        }

        List<Annotation> allAnnotations = new ArrayList<Annotation>();
        allAnnotations.addAll(actionParser.getAnnotations());
        allAnnotations.addAll(namespaceParser.getAnnotations());
        allAnnotations.addAll(parentPackageParser.getAnnotations());
        allAnnotations.addAll(resultParser.getAnnotations());
        allAnnotations.addAll(resultPathParser.getAnnotations());

        if (allAnnotations.size() == 0) {
            return;
        }

        registerClassAnnotations(allAnnotations);


        //  Some annotations have sub-annotations as parameters, these will have been parsed
        //      and attached to either the parent method or class, AND will have been
        //      parsed as children of the parent annotations. Collect the annotations
        //      that are members of another annotation, and remove them from the global
        //      annotation collection.

        List<Annotation> childAnnotations = list();
        for (Annotation genericAction : actionParser.getAnnotations()) {
            ActionAnnotation action = (ActionAnnotation)genericAction;
            childAnnotations.addAll(action.getResults());
        }

        allAnnotations.removeAll(childAnnotations);

        //  Assign annotations to their attached methods

        //  Generate method map
        Map<String, StrutsMethod> methodNameMap = new HashMap<String, StrutsMethod>();
        for (StrutsMethod method : classSigParser.getParsedMethods()) {
            String methodName = method.getName();
            if (methodNameMap.containsKey(methodName)) {
                LOG.debug("Multiple methods named " + methodName + " were found in \"" + file.getAbsolutePath() + "\", only the first will be used.");
            } else {
                methodNameMap.put(methodName, method);
            }
        }

        for (Annotation annotation : allAnnotations) {

            if (annotation.getTargetType() != Annotation.TargetType.METHOD) {
                continue;
            }

            String targetName = annotation.getTargetName();
            if (!methodNameMap.containsKey(targetName)) {
                LOG.debug("A " + annotation.getClass().getName() + " annotation was attached to "
                        + className + "." + targetName + ", but no parsed method could be found with that name.");
            } else {
                StrutsMethod attachedMethod = methodNameMap.get(targetName);
                attachedMethod.addAnnotation(annotation);
            }
        }
    }

    public StrutsClass getResultClass() {
        return resultClass;
    }



    private Set<ModelField> collectParameters(Collection<StrutsMethod> methods) {
        Map<String, String> propertyTypesWithSetters = map();
        Map<String, String> propertyTypesWithGetters = map();

        Set<ModelField> result = set();

        for (StrutsMethod method : methods) {
            String name = method.getName();
            if (name.startsWith("get")) {
                propertyTypesWithGetters.put(name.substring(3), method.getReturnType());
            } else if (name.startsWith("is")) {
                propertyTypesWithGetters.put(name.substring(2), method.getReturnType());
            } else if (name.startsWith("set")) {
                List<String> paramNames = new ArrayList<String>(method.getParameterNames());
                if (paramNames.size() > 0) {
                    String parameterType = method.getParameters().get(paramNames.get(0));
                    propertyTypesWithSetters.put(name.substring(3), parameterType);
                }
            }
        }

        for (String property : propertyTypesWithSetters.keySet()) {
            // Any method as a 'setter' on its own is enough to be treated as a property
//            if (!propertyTypesWithGetters.containsKey(property)) {
//                continue;
//            }

            String setterType = propertyTypesWithSetters.get(property);
            String getterType = propertyTypesWithGetters.get(property);

            // Parameters are parsed more reliably than return types
            String propertyType = setterType;
            ModelField newField = new ModelField(propertyType, property);
            result.add(newField);
        }

        return result;
    }

    private void registerClassAnnotations(Collection<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.getTargetType() == Annotation.TargetType.CLASS) {
                resultClass.addAnnotation(annotation);
            }
        }
    }
}
