package com.denimgroup.threadfix.framework.impl.django.python;

public class PythonVariableModification extends AbstractPythonStatement {

    String targetVariable;
    String value;
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
        return "change " + targetVariable + " by " + modificationType + " at line " + getSourceCodeLine();
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
