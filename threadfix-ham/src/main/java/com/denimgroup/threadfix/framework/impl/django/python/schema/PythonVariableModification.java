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

public class PythonVariableModification extends AbstractPythonStatement {

    String targetVariable;
    String value;
    String manualName = null;
    VariableModificationType modificationType = VariableModificationType.UNKNOWN;
    AbstractPythonStatement resolvedTarget;

    public void setTarget(String fullTargetVariableName) {
        this.targetVariable = fullTargetVariableName;
    }

    public void setOperatorValue(String value) {
        this.value = value;
    }

    public void setModificationType(VariableModificationType modificationType) {
        this.modificationType = modificationType;
    }

    @Override
    public String getName() {
        if (manualName == null) {
            return "change " + targetVariable + " by " + modificationType + " at line " + getSourceCodeStartLine();
        } else {
            return manualName;
        }
    }

    @Override
    public void setName(String newName) {
        manualName = newName;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonVariableModification clone = new PythonVariableModification();
        baseCloneTo(clone);
        clone.targetVariable = this.targetVariable;
        clone.value = this.value;
        clone.modificationType = this.modificationType;
        clone.resolvedTarget = this.resolvedTarget;
        clone.manualName = this.manualName;
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitVariableModifier(this);
        super.accept(visitor);
    }

    public String getTarget() {
        return targetVariable;
    }

    public String getOperatorValue() {
        return this.value;
    }

    public VariableModificationType getModificationType() {
        return modificationType;
    }

    public void setResolvedTarget(AbstractPythonStatement resolvedTarget) {
        this.resolvedTarget = resolvedTarget;
    }

    public AbstractPythonStatement getResolvedTarget() {
        return resolvedTarget;
    }
}
