package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonClass {

    List<String> baseTypes = list();
    String name;
    int lineNumber;
    List<PythonFunction> functions = list();
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

    public String getName() {
        return name;
    }

    public Collection<PythonFunction> getFunctions() {
        return functions;
    }

    public void addFunction(PythonFunction function) {
        functions.add(function);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
