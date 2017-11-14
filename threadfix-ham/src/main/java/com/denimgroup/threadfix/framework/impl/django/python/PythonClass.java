package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonClass extends AbstractPythonScope {

    List<String> baseTypes = list();
    String name;
    List<PythonDecorator> decorators = list();

    public Collection<String> getBaseTypes() {
        return baseTypes;
    }

    public void addBaseType(String baseType) {
        baseTypes.add(baseType);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
