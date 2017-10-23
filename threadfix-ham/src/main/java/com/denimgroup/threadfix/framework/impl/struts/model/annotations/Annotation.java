package com.denimgroup.threadfix.framework.impl.struts.model.annotations;



public abstract class Annotation {

    String annotationTargetName;
    TargetType annotationTargetType = TargetType.UNKNOWN;


    public enum TargetType {
        UNKNOWN,
        CLASS,
        METHOD
    }

    final public void setTargetName(String methodOrClassName) {
        this.annotationTargetName = methodOrClassName;
    }

    final public void setTargetType(TargetType annotationTargetType) {
        this.annotationTargetType = annotationTargetType;
    }

    final public String getTargetName() {
        return annotationTargetName;
    }

    final public TargetType getTargetType() {
        return annotationTargetType;
    }

    public boolean validate() {
        return annotationTargetType != TargetType.UNKNOWN && annotationTargetName != null;
    }

}
