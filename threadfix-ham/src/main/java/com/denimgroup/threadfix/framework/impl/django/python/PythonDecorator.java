package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonDecorator {
    private String name;
    private List<String> params = list();

    public PythonDecorator clone() {
        PythonDecorator clone = new PythonDecorator();
        clone.name = this.name;
        clone.params.addAll(this.params);
        return clone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getParams() {
        return params;
    }

    public void addParam(String paramValue) {
        params.add(paramValue);
    }
}
