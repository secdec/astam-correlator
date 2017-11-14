package com.denimgroup.threadfix.framework.impl.django.python;

public interface PythonVisitor {

    void visitModule(PythonModule pyModule);
    void visitClass(PythonClass pyClass);
    void visitFunction(PythonFunction pyFunction);
    void visitPublicVariable(PythonPublicVariable pyVariable);

}
