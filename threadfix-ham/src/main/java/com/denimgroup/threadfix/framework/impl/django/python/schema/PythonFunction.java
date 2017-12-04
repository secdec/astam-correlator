package com.denimgroup.threadfix.framework.impl.django.python.schema;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.VariableModificationType;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction extends AbstractPythonStatement {
    String name;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    public List<String> getParams() {
        return params;
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
        PythonFunction clone = new PythonFunction();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.params.addAll(this.params);
        for (PythonDecorator decorator : decorators) {
            clone.addDecorator(decorator.clone());
        }
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitFunction(this);
        super.accept(visitor);
    }

    public boolean canInvoke() {
        return false;
    }

    public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonPublicVariable target, String[] params) {
        return null;
    }

    @Override
    public Map<String, String> getImports() {
        Map<String, String> thisImports = new HashMap<String, String>(super.getImports());
        thisImports.putAll(getParentStatement().getImports());
        return thisImports;
    }

    public PythonClass getOwnerClass() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonClass.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonClass)parent;
        }
    }

    public PythonFunction getOwnerFunction() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonFunction.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonFunction)parent;
        }
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void addParam(String paramName) {
        params.add(paramName);
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }

    public static class PythonFunctionCall extends AbstractPythonStatement {

        String manualName = null;
        String invokeeName = null;
        String assignee = null;
        String functionName = null;
        List<String> args = list();
        PythonPublicVariable resolvedInvokee;
        PythonFunction resolvedFunction;

        @Override
        public String getName() {
            if (manualName == null) {
                return "call " + functionName + " on " + invokeeName + " at line " + getSourceCodeStartLine();
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
            PythonFunctionCall clone = new PythonFunctionCall();
            baseCloneTo(clone);
            clone.manualName = this.manualName;
            clone.invokeeName = this.invokeeName;
            clone.functionName = this.functionName;
            clone.resolvedInvokee = this.resolvedInvokee;
            clone.resolvedFunction = this.resolvedFunction;
            clone.args.addAll(this.args);
            return clone;
        }

        @Override
        public void accept(AbstractPythonVisitor visitor) {
            visitor.visitFunctionCall(this);
            super.accept(visitor);
        }

        public String getInvokeeName() {
            return invokeeName;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setCall(String invokee, String functionName) {
            this.invokeeName = invokee;
            this.functionName = functionName;
        }

        public void setCall(String functionName) {
            this.invokeeName = null;
            this.functionName = functionName;
        }

        public PythonPublicVariable getResolvedInvokee() {
            return resolvedInvokee;
        }

        public PythonFunction getResolvedFunction() {
            return resolvedFunction;
        }

        public void setResolvedInvokee(PythonPublicVariable resolvedInvokee) {
            this.resolvedInvokee = resolvedInvokee;
        }

        public void setResolvedFunction(PythonFunction resolvedFunction) {
            this.resolvedFunction = resolvedFunction;
        }

        public void addParameter(String value) {
            args.add(value);
        }

        public void setParameters(List<String> args) {
            this.args = new ArrayList<String>(args);
        }

        public List<String> getParameters() {
            return args;
        }
    }

    public static class PythonVariableModification extends AbstractPythonStatement {

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
}
