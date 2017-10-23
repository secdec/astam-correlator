package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

public class ParentPackageAnnotation extends Annotation {

    private String packageName;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
