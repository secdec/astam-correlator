package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonScope;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonModule;

public abstract class AbstractDjangoApi implements DjangoApi {

    protected void tryAddScopes(PythonCodeCollection codebase, AbstractPythonScope baseScope) {
        AbstractPythonScope targetScope = codebase.findByFullName(baseScope.getFullName());
        if (targetScope == null) {
            codebase.add(baseScope);
            targetScope = baseScope;
        }

        for (AbstractPythonScope child : baseScope.getChildScopes()) {
            child.setParentScope(targetScope);
            tryAddScopeTree(codebase, child, targetScope);
        }
    }

    private void tryAddScopeTree(PythonCodeCollection codebase, AbstractPythonScope newScopes, AbstractPythonScope baseScope) {
        AbstractPythonScope targetScope = codebase.findByPartialName(baseScope, newScopes.getName());
        if (targetScope == null) {
            baseScope.addChildScope(newScopes);
            targetScope = newScopes;
        }

        for (AbstractPythonScope child : newScopes.getChildScopes()) {
            child.setParentScope(targetScope);
            tryAddScopeTree(codebase, child, targetScope);
        }
    }

    protected AbstractPythonScope getRootScope(AbstractPythonScope scope) {
        while (scope.getParentScope() != null) {
            scope = scope.getParentScope();
        }
        return scope;
    }

    protected PythonModule makeModulesFromFullName(String fullScopeName) {
        PythonModule currentModule = null;
        String[] parts = fullScopeName.split("\\.");
        for (String module : parts) {
            if (currentModule == null) {
                currentModule = new PythonModule();
                currentModule.setName(module);
            } else {
                PythonModule newModule = new PythonModule();
                newModule.setName(module);
                currentModule.addChildScope(newModule);
                currentModule = newModule;
            }
        }
        return currentModule;
    }

}
