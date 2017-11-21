package com.denimgroup.threadfix.framework.impl.django.python;

public abstract class AbstractPythonVisitor {

    public static void visitSingle(AbstractPythonVisitor visitor, AbstractPythonStatement statement) {
        Class<?> type = statement.getClass();
        if (PythonClass.class.isAssignableFrom(type)) {
            visitor.visitClass((PythonClass)statement);
        } else if (PythonFunction.class.isAssignableFrom(type)) {
            visitor.visitFunction((PythonFunction)statement);
        } else if (PythonModule.class.isAssignableFrom(type)) {
            visitor.visitModule((PythonModule)statement);
        } else if (PythonPublicVariable.class.isAssignableFrom(type)) {
            visitor.visitPublicVariable((PythonPublicVariable)statement);
        } else if (PythonVariableModification.class.isAssignableFrom(type)) {
            visitor.visitVariableModifier((PythonVariableModification)statement);
        } else if (PythonFunctionCall.class.isAssignableFrom(type)) {
            visitor.visitFunctionCall((PythonFunctionCall)statement);
        }
        visitor.visitAny(statement);
    }

    public void visitModule(PythonModule pyModule) {

    }

    public void visitClass(PythonClass pyClass) {

    }

    public void visitFunction(PythonFunction pyFunction) {

    }

    public void visitPublicVariable(PythonPublicVariable pyVariable) {

    }

    public void visitVariableModifier(PythonVariableModification pyModification) {

    }

    public void visitFunctionCall(PythonFunctionCall pyFunctionCall) {

    }

    public void visitAny(AbstractPythonStatement statement) {

    }

}
