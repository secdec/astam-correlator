package com.denimgroup.threadfix.framework.impl.django.python.schema;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonModule extends AbstractPythonStatement {

    String name;
    Map<String, AbstractPythonStatement> implicitImports = map();


    public Map<String, AbstractPythonStatement> getImplicitImports() {
        return implicitImports;
    }

    public void addImplicitImport(AbstractPythonStatement importedStatement, String alias) {
        implicitImports.put(alias, importedStatement);
    }

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

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitModule(this);
        super.accept(visitor);
    }

    @Override
    public AbstractPythonStatement findChild(String immediateChildName) {
        AbstractPythonStatement result = super.findChild(immediateChildName);
        if (result == null) {
            for (Map.Entry<String, AbstractPythonStatement> implicitImport : implicitImports.entrySet()) {
                if (implicitImport.getKey().equals(immediateChildName)) {
                    result = implicitImport.getValue();
                    break;
                }
            }
        }
        return result;
    }
}
