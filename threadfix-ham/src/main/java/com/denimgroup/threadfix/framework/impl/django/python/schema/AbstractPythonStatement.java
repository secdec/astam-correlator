////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


package com.denimgroup.threadfix.framework.impl.django.python.schema;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public abstract class AbstractPythonStatement {

    private AbstractPythonStatement parentStatement;
    private List<AbstractPythonStatement> childStatements = list();
    private String sourceCodePath;
    private int sourceCodeStartLine = -1;
    private int sourceCodeEndLine = -1;
    private Map<String, String> imports = map();
    private int indentationLevel = -1;

    public abstract String getName();
    public abstract void setName(String newName);
    public abstract AbstractPythonStatement clone();


    /**
     * Clones all children and shared properties to the target 'clone'.
     */
    protected AbstractPythonStatement baseCloneTo(AbstractPythonStatement clone) {
        clone.setName(this.getName());
        clone.setSourceCodeStartLine(this.getSourceCodeStartLine());
        clone.setSourceCodeEndLine(this.getSourceCodeEndLine());
        clone.setSourceCodePath(this.getSourceCodePath());
        for (AbstractPythonStatement child : childStatements) {
            clone.addChildStatement(child.clone());
        }
        return clone;
    }

    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    public String getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodeStartLine(int sourceCodeStartLine) {
        this.sourceCodeStartLine = sourceCodeStartLine;
    }

    public int getSourceCodeStartLine() {
        return sourceCodeStartLine;
    }

    public int getSourceCodeEndLine() {
        return sourceCodeEndLine;
    }

    public void setSourceCodeEndLine(int sourceCodeEndLine) {
        this.sourceCodeEndLine = sourceCodeEndLine;
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

    public List<AbstractPythonStatement> getChildStatements() {
        return childStatements;
    }

    public <T extends AbstractPythonStatement> List<T> getChildStatements(@Nonnull Class<T> type) {
        List<T> result = new LinkedList<T>();
        for (AbstractPythonStatement statement : getChildStatements()) {
            if (type.isAssignableFrom(statement.getClass())) {
                result.add((T)statement);
            }
        }
        return result;
    }

    public List<AbstractPythonStatement> getChildStatements(@Nonnull Class<?>... types) {
        List<AbstractPythonStatement> result = list();
        for (AbstractPythonStatement statement : getChildStatements()) {
            for (Class<?> type : types) {
                if (type.isAssignableFrom(statement.getClass())) {
                    result.add(statement);
                }
            }
        }
        return result;
    }

    public AbstractPythonStatement findChild(String immediateChildName) {
        for (AbstractPythonStatement statement : getChildStatements()) {
            if (statement.getName().equals(immediateChildName)) {
                return statement;
            }
        }
        return null;
    }

    public <T extends AbstractPythonStatement> Collection<T> findChildren(Class<T> type) {
        List<T> result = list();
        for (AbstractPythonStatement statement : getChildStatements()) {
            if (type.isAssignableFrom(statement.getClass())) {
                result.add((T)statement);
            }
        }
        return result;
    }

    public <T extends AbstractPythonStatement> T findChild(String immediateChildName, Class<T> type) {
        AbstractPythonStatement result = findChild(immediateChildName);
        if (result == null) {
            return null;
        } else {
            if (type.isAssignableFrom(result.getClass())) {
                return (T)result;
            } else {
                return null;
            }
        }
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
        visitor.visitAny(this);
        for (AbstractPythonStatement statement : childStatements) {
            statement.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getFullName() + " - " + getSourceCodePath();
    }
}
