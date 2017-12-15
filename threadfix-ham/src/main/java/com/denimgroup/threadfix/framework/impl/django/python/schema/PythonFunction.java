package com.denimgroup.threadfix.framework.impl.django.python.schema;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.VariableModificationType;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonVariable;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction extends AbstractPythonStatement {
    String name;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    public List<String> getParams() {
        return params;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonFunction clone = new PythonFunction();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.params.addAll(this.params);
        for (PythonDecorator decorator : decorators) {
            clone.addDecorator(decorator.clone());
        }
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitFunction(this);
        super.accept(visitor);
    }

    public boolean canInvoke() {
        return false;
    }

    public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonValue target, PythonValue[] params) {
        return null;
    }

    @Override
    public Map<String, String> getImports() {
        Map<String, String> thisImports = new HashMap<String, String>(super.getImports());
        thisImports.putAll(getParentStatement().getImports());
        return thisImports;
    }

    public PythonClass getOwnerClass() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonClass.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonClass)parent;
        }
    }

    public PythonFunction getOwnerFunction() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonFunction.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonFunction)parent;
        }
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void addParam(String paramName) {
        params.add(paramName);
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
