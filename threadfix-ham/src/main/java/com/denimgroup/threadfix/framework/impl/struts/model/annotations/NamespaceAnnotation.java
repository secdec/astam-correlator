package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

//  See: https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Namespaceannotation

public class NamespaceAnnotation extends Annotation {
    String path;

    public void setNamespacePath(String path) {
        this.path = path;
    }

    public String getNamespacePath() {
        return this.path;
    }
}
