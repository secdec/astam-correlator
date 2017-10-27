package com.denimgroup.threadfix.framework.impl.struts.model;

import com.denimgroup.threadfix.framework.impl.model.ModelField;
import com.denimgroup.threadfix.framework.impl.model.ModelFieldSet;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.*;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsClass {


    public static final List<String> AcceptedStrutsActionBaseClasses = list(
            "ActionSupport",
            "ModelDriven\\<\\w+\\>",
            "ValidationAwareSupport"
    );

    public StrutsClass(String className, String sourceFile) {
        this.className = className;
        this.sourceFilePath = sourceFile;
    }

    private String sourceFilePath;
    private String className;
    private List<String> baseTypes = list();
    private List<Annotation> attachedAnnotations = list();
    private List<StrutsMethod> methods = list();
    private List<String> importedPackages = list();
    private String sourcePackage = null;
    private ModelFieldSet properties = null;

    public String getName() {
        return className;
    }

    public String getCleanName() {
        return className
                .replaceAll("Action$", "");
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

    public void setProperties(ModelFieldSet properties) {
        this.properties = properties;
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

    public Collection<String> getImportedPackages() {
        return this.importedPackages;
    }

    public Collection<String> getBaseTypes() {
        return baseTypes;
    }

    public Collection<Annotation> getAllAnnotations() {
        return attachedAnnotations;
    }

    public ModelFieldSet getProperties() {
        return properties;
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
