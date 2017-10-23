package com.denimgroup.threadfix.framework.impl.struts.model;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ActionAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultPathAnnotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsClass {


    public static final List<String> AcceptedStrutsActionBaseClasses = list(
            "ActionSupport",
            "ModelDriven",
            "ValidationAwareSupport"
    );

    public StrutsClass(String className, String sourceFile) {
        this.className = className;
    }

    private String sourceFilePath;
    private String className;
    private List<String> baseTypes = list();
    private List<Annotation> attachedAnnotations = list();

    public String getName() {
        return className;
    }

    public String getSourceFile() {
        return sourceFilePath;
    }

    public Collection<String> getBaseTypes() {
        return baseTypes;
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

    public Collection<Annotation> getAllAnnotations() {
        return attachedAnnotations;
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

}
