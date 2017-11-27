package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonClass extends AbstractPythonStatement {

    List<String> baseTypes = list();
    String name;
    List<PythonDecorator> decorators = list();

    @Override
    public Map<String, String> getImports() {
        Map<String, String> result = new HashMap<String, String>(this.getParentStatement().getImports());
        result.putAll(super.getImports());
        return result;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonClass clone = new PythonClass();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.baseTypes.addAll(this.baseTypes);
        for (PythonDecorator decorator : decorators) {
            clone.addDecorator(decorator.clone());
        }
        return clone;
    }

    public Collection<String> getBaseTypes() {
        return baseTypes;
    }

    public void addBaseType(String baseType) {
        baseTypes.add(baseType);
    }

    public void setBaseTypes(Collection<String> baseTypes) {
        this.baseTypes.addAll(baseTypes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }



    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
