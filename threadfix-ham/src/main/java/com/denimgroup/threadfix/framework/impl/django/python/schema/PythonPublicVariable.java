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

import com.denimgroup.threadfix.framework.impl.django.python.VariableModificationType;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.util.Collection;
import java.util.Map;

public class PythonPublicVariable extends AbstractPythonStatement {

    String name;
    String valueString;
    PythonClass resolvedTypeClass;
    boolean isArray = false;

    public void setResolvedTypeClass(PythonClass pyClass) {
        resolvedTypeClass = pyClass;
    }

    public PythonClass getResolvedTypeClass() {
        return resolvedTypeClass;
    }

    @Override
    public AbstractPythonStatement findChild(String immediateChildName) {
        AbstractPythonStatement result = super.findChild(immediateChildName);
        if (result == null && resolvedTypeClass != null) {
            result = resolvedTypeClass.findChild(immediateChildName);
        }
        return result;
    }

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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonPublicVariable clone = new PythonPublicVariable();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.valueString = this.valueString;
        clone.resolvedTypeClass = this.resolvedTypeClass;
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitPublicVariable(this);
        super.accept(visitor);
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;

        isArray = valueString.startsWith("[") && valueString.length() > 1;

        this.clearChildStatements();

        if (isArray) {
            valueString = valueString.substring(1, valueString.length() - 1);
            String[] values = CodeParseUtil.splitByComma(valueString);
            int i = 0;
            for (String value : values) {
                int idx = i++;

                PythonPublicVariable element = new PythonPublicVariable();
                element.setSourceCodeStartLine(this.getSourceCodeStartLine());
                element.setSourceCodePath(this.getSourceCodePath());
                element.setName("[" + idx + "]");
                element.setValueString(value);

                this.addChildStatement(element);

                PythonVariableModification assignment = new PythonVariableModification();
                assignment.setTarget(element.getFullName());
                assignment.setSourceCodeStartLine(this.getSourceCodeStartLine());
                assignment.setSourceCodePath(this.getSourceCodePath());
                assignment.setName("Array[" + idx + "] = " + value);
                assignment.setOperatorValue(value);
                assignment.setModificationType(VariableModificationType.ASSIGNMENT);

                this.addChildStatement(assignment);
            }
        }
    }

    public String getValueString() {
        return valueString;
    }
}
