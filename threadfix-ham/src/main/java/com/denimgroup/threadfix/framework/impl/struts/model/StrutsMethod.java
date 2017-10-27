package com.denimgroup.threadfix.framework.impl.struts.model;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsMethod {

    String methodName;
    List<String> parameters = list();
    List<Annotation> annotations = list();



    public String getName() {
        return methodName;
    }

    public String getUniqueName() {
        StringBuilder uniqueName = new StringBuilder();
        uniqueName.append(methodName);

        for (String param : parameters) {
            uniqueName.append('_');
            uniqueName.append(param);
        }

        return uniqueName.toString();
    }

    public Collection<String> getParameters() {
        return parameters;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }



    public void setName(String name) {
        methodName = name;
    }

    public void addParameter(String name) {
        parameters.add(name);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (annotations.size() > 0) {
            sb.append("@(");
            for (int i = 0; i < annotations.size(); i++) {
                Annotation annotation = annotations.get(i);
                sb.append(annotation.toString());
                if (i < annotations.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");
        }

        sb.append(methodName);
        sb.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
