package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ParentPackageAnnotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ParentPackageAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> processedAnnotations = list();
    List<ParentPackageAnnotation> pendingAnnotations = list();

    ParentPackageAnnotation currentAnnotation;


    @Override
    public Collection<Annotation> getAnnotations() {
        return processedAnnotations;
    }

    @Override
    protected String getAnnotationName() {
        return "ParentPackage";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentAnnotation = new ParentPackageAnnotation();
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType) {
        for (ParentPackageAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        processedAnnotations.addAll(pendingAnnotations);
        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex) {
        if (parameterIndex == 0) {
            currentAnnotation.setPackageName(value);
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value) {
        if (name.equals("value")) {
            currentAnnotation.setPackageName(value);
        }
    }
}
