package com.denimgroup.threadfix.framework.impl.django.python;

public class PythonModule extends AbstractPythonScope {

    String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
