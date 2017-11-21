package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonModule;

public abstract class AbstractDjangoApi implements DjangoApi {

    protected void tryAddScopes(PythonCodeCollection codebase, AbstractPythonStatement baseScope) {
        AbstractPythonStatement targetScope = codebase.findByFullName(baseScope.getFullName());
        if (targetScope == null) {
            codebase.add(baseScope);
            targetScope = baseScope;
        }

        for (AbstractPythonStatement child : baseScope.getChildStatements()) {
            child.setParentStatement(targetScope);
            tryAddScopeTree(codebase, child, targetScope);
        }
    }

    private void tryAddScopeTree(PythonCodeCollection codebase, AbstractPythonStatement newScopes, AbstractPythonStatement baseScope) {
        AbstractPythonStatement targetScope = codebase.findByPartialName(baseScope, newScopes.getName());
        if (targetScope == null) {
            baseScope.addChildStatement(newScopes);
            targetScope = newScopes;
        }

        for (AbstractPythonStatement child : newScopes.getChildStatements()) {
            child.setParentStatement(targetScope);
            tryAddScopeTree(codebase, child, targetScope);
        }
    }

    protected AbstractPythonStatement getRootScope(AbstractPythonStatement scope) {
        while (scope.getParentStatement() != null) {
            scope = scope.getParentStatement();
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
                currentModule.addChildStatement(newModule);
                currentModule = newModule;
            }
        }
        return currentModule;
    }

}
