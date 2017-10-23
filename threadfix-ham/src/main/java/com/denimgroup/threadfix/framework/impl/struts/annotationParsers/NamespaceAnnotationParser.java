package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.NamespaceAnnotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class NamespaceAnnotationParser extends AbstractAnnotationParser {

    List<Annotation> parsedNamespaces = list();
    List<NamespaceAnnotation> pendingNamespaces = list();

    NamespaceAnnotation currentNamespace = null;

    @Override
    public Collection<Annotation> getAnnotations() {
        return parsedNamespaces;
    }

    @Override
    protected String getAnnotationName() {
        return "Namespace";
    }

    @Override
    protected void onAnnotationFound(int type, int lineNumber, String stringValue) {
        currentNamespace = new NamespaceAnnotation();
    }

    @Override
    protected void onParsingEnded(int type, int lineNumber, String stringValue) {
        pendingNamespaces.add(currentNamespace);
        currentNamespace = null;
    }

    @Override
    protected void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType) {
        for (NamespaceAnnotation annotation : pendingNamespaces) {
            annotation.setTargetName(targetName);
            annotation.setTargetType(targetType);
        }

        parsedNamespaces.addAll(pendingNamespaces);
        pendingNamespaces.clear();
    }

    @Override
    protected void onAnnotationParameter(String value, int parameterIndex) {
        if (parameterIndex == 0) {
            currentNamespace.setNamespacePath(value);
        }
    }

    @Override
    protected void onNamedAnnotationParameter(String name, String value) {
        if (name.equals("value")) {
            currentNamespace.setNamespacePath(value);
        }
    }
}
