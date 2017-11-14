package com.denimgroup.threadfix.framework.impl.django.python;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class AbstractPythonScope {

    private AbstractPythonScope parentScope;
    private List<AbstractPythonScope> childScopes = list();
    private String sourceCodePath;
    private int sourceCodeLine;

    public abstract String getName();

    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    public String getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodeLine(int sourceCodeLine) {
        this.sourceCodeLine = sourceCodeLine;
    }

    public int getSourceCodeLine() {
        return sourceCodeLine;
    }

    public void setParentModule(AbstractPythonScope parentModule) {
        this.parentScope = parentModule;
        if (!parentModule.childScopes.contains(this)) {
            parentModule.childScopes.add(this);
        }
    }

    public AbstractPythonScope getParentScope() {
        return parentScope;
    }

    public void addChildScope(AbstractPythonScope newChild) {
        newChild.setParentModule(this);
    }

    public Collection<AbstractPythonScope> getChildScopes() {
        return childScopes;
    }

    public <T extends AbstractPythonScope> Collection<T> getChildScopes(@Nonnull Class<T> type) {
        List<T> result = new LinkedList<T>();
        for (AbstractPythonScope scope : childScopes) {
            if (type.isAssignableFrom(scope.getClass())) {
                result.add((T)scope);
            }
        }
        return result;
    }


    public String getFullName() {
        List<AbstractPythonScope> parentChain = list();
        AbstractPythonScope currentScope = this;
        while (currentScope != null) {
            parentChain.add(currentScope);
            currentScope = currentScope.getParentScope();
        }
        Collections.reverse(parentChain);
        StringBuilder fullName = new StringBuilder();
        for (AbstractPythonScope scope : parentChain) {
            if (fullName.length() > 0) {
                fullName.append('.');
            }
            fullName.append(scope.getName());
        }
        return fullName.toString();
    }

    public void accept(PythonVisitor visitor) {
        Collection<PythonClass> classes = getChildScopes(PythonClass.class);
        Collection<PythonFunction> functions = getChildScopes(PythonFunction.class);
        Collection<PythonModule> modules = getChildScopes(PythonModule.class);
        Collection<PythonPublicVariable> variables = getChildScopes(PythonPublicVariable.class);

        for (PythonClass pyClass : classes) {
            visitor.visitClass(pyClass);
            pyClass.accept(visitor);
        }

        for (PythonFunction pyFunction : functions) {
            visitor.visitFunction(pyFunction);
            pyFunction.accept(visitor);
        }

        for (PythonModule pyModule : modules) {
            visitor.visitModule(pyModule);
            pyModule.accept(visitor);
        }

        for (PythonPublicVariable pyVariable : variables) {
            visitor.visitPublicVariable(pyVariable);
            pyVariable.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getFullName() + " - " + getSourceCodePath();
    }
}
