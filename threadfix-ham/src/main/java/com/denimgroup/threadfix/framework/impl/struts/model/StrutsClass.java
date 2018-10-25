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


package com.denimgroup.threadfix.framework.impl.struts.model;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.set;

public class StrutsClass {


    public static final List<String> AcceptedStrutsActionBaseClasses = list(
            "ActionSupport",
            "ModelDriven\\<\\w+\\>",
            "ValidationAwareSupport"
    );

    public StrutsClass(String className, String sourceFile) {
        this.className = className;

        if (sourceFile != null) {
        	sourceFile = StringUtils.replaceChars(sourceFile, '\\', '/');
        }
        this.sourceFilePath = sourceFile;
    }

    private String sourceFilePath;
    private String className;
    private List<String> baseTypes = list();
    private List<Annotation> attachedAnnotations = list();
    private List<StrutsMethod> methods = list();
    private List<String> importedPackages = list();
    private String sourcePackage = null;
    private Set<ModelField> properties = set();
    private Set<ModelField> fields = set();

    public String getName() {
        return className;
    }

    public String getCleanName() {
        return className
                .replaceAll("Action$", "")
                .replaceAll("<.*>$", "");
    }

    public String getCleanName(String[] filters) {
        String result = className;
        for (String filter : filters) {
            result = result.replaceAll(filter + "$", "");
        }
        return result;
    }



    public void setPackage(String fullSourcePackageName) {
        sourcePackage = fullSourcePackageName;
    }

    public void addMethod(StrutsMethod method) {
        methods.add(method);
    }

    public void addAllMethods(Collection<StrutsMethod> methods) {
        this.methods.addAll(methods);
    }

    public void setImportedPackages(Collection<String> packages) {
        this.importedPackages.clear();
        this.importedPackages.addAll(packages);
    }

    public void addBaseType(String typeName) {
        baseTypes.add(typeName);
    }

    public void addAnnotation(Annotation annotation) {
        this.attachedAnnotations.add(annotation);
    }

    public void addAllAnnotations(Collection<Annotation> annotations) {
        this.attachedAnnotations.addAll(annotations);
    }

    public void setProperties(Set<ModelField> properties) {
        this.properties = properties;
    }

    public void setFields(Set<ModelField> fields) {
        this.fields = fields;
    }

    public void addField(ModelField field) {
        this.fields.add(field);
    }

    public String getPackage() {
        return sourcePackage;
    }

    public String getSourceFile() {
        return sourceFilePath;
    }

    public Collection<StrutsMethod> getMethods() {
        return methods;
    }

    public StrutsMethod getMethod(String name) {
        for (StrutsMethod method : methods) {
            if (method.getName().equalsIgnoreCase(name)) {
                return method;
            }
        }
        return null;
    }

    public Collection<String> getImportedPackages() {
        return this.importedPackages;
    }

    public Collection<String> getBaseTypes() {
        return baseTypes;
    }

    public Collection<Annotation> getAllAnnotations() {
        return attachedAnnotations;
    }

    public Set<ModelField> getProperties() {
        return properties;
    }

    public ModelField getProperty(String name) {
        for (ModelField field : properties) {
            if (field.getParameterKey().equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }

    public Set<ModelField> getFields() {
        return fields;
    }

    public ModelField getField(String name) {
        for (ModelField field : fields) {
            if (field.getParameterKey().equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }

    public boolean hasField(String fieldName) {
        return getField(fieldName) != null;
    }

    public boolean hasFieldOrProperty(String name) {
        return getField(name) != null || getProperty(name) != null;
    }

    public ModelField getFieldOrProperty(String name) {
        ModelField result;
        result = getField(name);
        if (result == null) {
            result = getProperty(name);
        }
        return result;
    }

    public Set<ModelField> getFieldsAndProperties() {
        Set<ModelField> result = set();
        result.addAll(fields);
        result.addAll(properties);
        return result;
    }

    public boolean hasBaseType(String baseTypeName) {

        boolean isAbsoluteReference = baseTypeName.contains(".");

        for (String baseType : this.baseTypes) {
            if (!isAbsoluteReference) {
                //  Check for type name directly
                Matcher typeMatcher = Pattern.compile(baseType).matcher(baseTypeName);
                if (typeMatcher.find()) {
                    return true;
                }
            } else {
                for (String importedPackage : importedPackages) {
                    String fullyQualifiedType = importedPackage + "." + baseType;
                    Matcher typeMatcher = Pattern.compile(fullyQualifiedType).matcher(baseTypeName);
                    if (typeMatcher.find()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }



    public Collection<Annotation> getAllAnnotations(Annotation.TargetType annotationTargetType) {
        List<Annotation> annotations = list();
        for (Annotation annotation : attachedAnnotations) {
            if (annotation.getTargetType() == annotationTargetType) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    public <AnnotationType extends Annotation> Collection<AnnotationType> getAnnotations(Class<AnnotationType> type) {
        List<AnnotationType> annotations = list();
        for (Annotation annotation : attachedAnnotations) {
            if (annotation.getClass() == type) {
                annotations.add((AnnotationType)annotation);
            }
        }
        return annotations;
    }

    public <AnnotationType extends Annotation> Collection<AnnotationType> getAnnotations(Class<AnnotationType> type, Annotation.TargetType annotationTargetType) {
        List<AnnotationType> annotations = list();
        for (Annotation annotation : attachedAnnotations) {
            if (annotation.getTargetType() == annotationTargetType && annotation.getClass() == type) {
                annotations.add((AnnotationType)annotation);
            }
        }
        return annotations;
    }

    public <AnnotationType extends Annotation> AnnotationType getFirstAnnotation(Class<AnnotationType> type) {
        for (Annotation annotation : attachedAnnotations) {
            if (annotation.getClass() == type) {
                return (AnnotationType)annotation;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sourcePackage);
        sb.append(".");
        sb.append(className);
        if (baseTypes.size() > 0) {
            sb.append(" (");
            for (int i = 0; i < baseTypes.size(); i++) {
                String baseType = baseTypes.get(i);
                sb.append(baseType);
                if (i < baseTypes.size() - 1);
                    sb.append(", ");
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
