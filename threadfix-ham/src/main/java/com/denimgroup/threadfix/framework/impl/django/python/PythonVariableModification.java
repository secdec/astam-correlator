package com.denimgroup.threadfix.framework.impl.django.python;

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
            return "change " + targetVariable + " by " + modificationType + " at line " + getSourceCodeLine();
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
