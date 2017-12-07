package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class ExecutionContext {

    PythonCodeCollection codebase;
    Map<String, PythonValue> workingMemory = map();
    PythonValue selfValue;
    AbstractPythonStatement scope;
    ExecutionContext parentContext = null;

    public ExecutionContext(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue) {
        this.codebase = codebase;
        this.selfValue = selfValue;
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue, AbstractPythonStatement scope) {
        this.codebase = codebase;
        this.selfValue = selfValue;
        this.scope = scope;
    }

    public PythonValue getSelfValue() {
        return selfValue;
    }

    public void setSelfValue(PythonValue selfValue) {
        this.selfValue = selfValue;
    }

    public Map<String, PythonValue> getWorkingMemory() {
        return workingMemory;
    }

    public AbstractPythonStatement getScope() {
        return scope;
    }

    public void setScope(AbstractPythonStatement scope) {
        this.scope = scope;
    }

    public ExecutionContext getParentContext() {
        return parentContext;
    }

    public void setParentContext(ExecutionContext parentContext) {
        this.parentContext = parentContext;
    }

    public PythonValue resolveSymbol(String symbolName) {
        AbstractPythonStatement codebaseLocation = codebase.resolveLocalSymbol(symbolName, scope);
        if (codebaseLocation != null) {
            String fullName = codebaseLocation.getFullName();
            return findSymbol(fullName);
        } else {
            return null;
        }
    }

    public PythonCodeCollection getCodebase() {
        return codebase;
    }

    private PythonValue findSymbol(String fullSymbolName) {
        if (workingMemory.containsKey(fullSymbolName)) {
            return workingMemory.get(fullSymbolName);
        } else if (parentContext != null) {
            return parentContext.findSymbol(fullSymbolName);
        } else {
            return null;
        }
    }
}
