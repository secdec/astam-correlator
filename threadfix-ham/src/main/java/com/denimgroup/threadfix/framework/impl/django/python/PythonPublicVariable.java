package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Map;

public class PythonPublicVariable extends AbstractPythonScope {

    String name;
    String typeName;

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

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
