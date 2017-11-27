package com.denimgroup.threadfix.framework.impl.django.python;

public class PythonModule extends AbstractPythonStatement {

    String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonModule clone = new PythonModule();
        baseCloneTo(clone);
        clone.name = this.name;
        return clone;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
