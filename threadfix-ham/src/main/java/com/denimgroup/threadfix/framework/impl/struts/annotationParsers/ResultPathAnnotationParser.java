package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.ResultPathAnnotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ResultPathAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> processedAnnotations = list();
    List<ResultPathAnnotation> pendingAnnotations = list();
    ResultPathAnnotation currentAnnotation;

    @Override
    public Collection<Annotation> getAnnotations() {
        return processedAnnotations;
    }

    @Override
    protected String getAnnotationName() {
        return "ResultPath";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentAnnotation = new ResultPathAnnotation();
        currentAnnotation.setCodeLine(lineNumber);
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingAnnotations.add(currentAnnotation);
        processedAnnotations.add(currentAnnotation);
        currentAnnotation = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType, int lineNumber) {
        for (ResultPathAnnotation annotation : pendingAnnotations) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        pendingAnnotations.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex, int lineNumber) {
        if (parameterIndex == 0) {
            currentAnnotation.setLocation(value);
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value, int lineNumber) {
        if (name.equals("value")) {
            currentAnnotation.setLocation(value);
        }
    }
}
