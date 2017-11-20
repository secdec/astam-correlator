package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Map;

public class PythonPublicVariable extends AbstractPythonScope {

    String name;
    String valueString;

    @Override
    public void addImport(String importedItem, String alias) {

    }

    @Override
    public Map<String, String> getImports() {
        return this.findParent(PythonModule.class).getImports();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public String getValueString() {
        return valueString;
    }
}
