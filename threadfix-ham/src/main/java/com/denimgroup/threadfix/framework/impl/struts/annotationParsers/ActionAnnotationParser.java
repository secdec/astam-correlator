package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ActionAnnotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ActionAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> processedAnnotations = list();
    List<ActionAnnotation> pendingAnnotations = list();
    ActionAnnotation currentAnnotation;

    public Collection<Annotation> getAnnotations() {
        return processedAnnotations;
    }

    @Override
    protected String getAnnotationName() {
        return "Action";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentAnnotation = new ActionAnnotation();
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType) {

        for (ActionAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        processedAnnotations.addAll(pendingAnnotations);
        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex) {
        switch (parameterIndex) {
            case 0:
                currentAnnotation.setBoundUrl(value);
                break;
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value) {
        if (name.equals("value")) {
            currentAnnotation.setBoundUrl(value);
        }
    }
}
