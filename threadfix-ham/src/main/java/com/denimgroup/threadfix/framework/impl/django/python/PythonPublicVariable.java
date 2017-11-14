package com.denimgroup.threadfix.framework.impl.django.python;

public class PythonPublicVariable extends AbstractPythonScope {

    String name;
    String typeName;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
