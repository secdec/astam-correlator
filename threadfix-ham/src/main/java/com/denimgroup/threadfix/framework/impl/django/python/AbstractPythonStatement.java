package com.denimgroup.threadfix.framework.impl.django.python;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public abstract class AbstractPythonStatement {

    private AbstractPythonStatement parentStatement;
    private List<AbstractPythonStatement> childStatements = list();
    private String sourceCodePath;
    private int sourceCodeLine;
    private Map<String, String> imports = map();
    private Map<String, String> urlsModifications = map();
    private int indentationLevel = -1;

    public abstract String getName();
    public abstract void setName(String newName);
    public abstract AbstractPythonStatement clone();


    /**
     * Clones all children and shared properties to the target 'clone'.
     */
    protected void baseCloneTo(AbstractPythonStatement clone) {
        clone.setName(this.getName());
        clone.setSourceCodeLine(this.getSourceCodeLine());
        clone.setSourceCodePath(this.getSourceCodePath());
        for (AbstractPythonStatement child : childStatements) {
            clone.addChildStatement(child.clone());
        }
    }

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

    public void addUrlModification(String endpoint, String targetController) {
        urlsModifications.put(endpoint, targetController);
    }

    public Map<String, String> getUrlsModifications() {
        return urlsModifications;
    }

    public void setIndentationLevel(int indentationLevel) {
        this.indentationLevel = indentationLevel;
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public void addImport(String importedItem, String alias) {
        if (imports.containsKey(alias)) {
            String existingImport = imports.get(alias);
            imports.put(alias, existingImport + "|" + importedItem);
        } else {
            imports.put(alias, importedItem);
        }
    }

    public String resolveImportedAlias(String alias) {
        return imports.get(alias);
    }

    public void setParentStatement(AbstractPythonStatement parentModule) {

        if (this.parentStatement != null) {
            this.parentStatement.childStatements.remove(this);
        }

        if (parentModule == null) {
            this.parentStatement = null;
            return;
        }

        assert this != parentModule: "Cannot set parent to itself!";
        this.parentStatement = parentModule;
        if (!parentModule.childStatements.contains(this)) {
            parentModule.childStatements.add(this);
        }
    }

    public AbstractPythonStatement getParentStatement() {
        return parentStatement;
    }

    public void addChildStatement(AbstractPythonStatement newChild) {
        newChild.setParentStatement(this);
    }

    public void removeChildStatement(AbstractPythonStatement childStatement) {
        if (this.childStatements.contains(childStatement)) {
            childStatement.parentStatement = null;
            this.childStatements.remove(childStatement);
        }
    }

    public void clearChildStatements() {
        while (childStatements.size() > 0) {
            AbstractPythonStatement child = childStatements.get(0);
            child.setParentStatement(null);
        }
    }

    public void detach() {
        if (this.parentStatement != null) {
            this.parentStatement.removeChildStatement(this);
        }
    }

    public Collection<AbstractPythonStatement> getChildStatements() {
        return childStatements;
    }

    public <T extends AbstractPythonStatement> Collection<T> getChildStatements(@Nonnull Class<T> type) {
        List<T> result = new LinkedList<T>();
        for (AbstractPythonStatement statement : childStatements) {
            if (type.isAssignableFrom(statement.getClass())) {
                result.add((T)statement);
            }
        }
        return result;
    }

    public AbstractPythonStatement findChild(String immediateChildName) {
        for (AbstractPythonStatement statement : childStatements) {
            if (statement.getName().equals(immediateChildName)) {
                return statement;
            }
        }
        return null;
    }


    public String getFullName() {
        List<AbstractPythonStatement> parentChain = list();
        AbstractPythonStatement currentStatement = this;
        while (currentStatement != null) {
            parentChain.add(currentStatement);
            currentStatement = currentStatement.getParentStatement();
        }
        Collections.reverse(parentChain);
        StringBuilder fullName = new StringBuilder();
        for (AbstractPythonStatement statement : parentChain) {
            if (fullName.length() > 0) {
                fullName.append('.');
            }
            fullName.append(statement.getName());
        }
        return fullName.toString();
    }

    public <T extends AbstractPythonStatement> T findParent(Class<T> type) {
        AbstractPythonStatement current = this.getParentStatement();
        while (current != null) {
            if (type.isAssignableFrom(current.getClass())) {
                return (T)current;
            }
            current = current.getParentStatement();
        }
        return null;
    }

    public void accept(AbstractPythonVisitor visitor) {
        for (AbstractPythonStatement statement : childStatements) {
            AbstractPythonVisitor.visitSingle(visitor, statement);
            statement.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getFullName() + " - " + getSourceCodePath();
    }
}
