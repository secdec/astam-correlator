package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;

public interface PythonExpression extends PythonValue {

    int getScopingIndentation();
    void setScopingIndentation(int indentation);

    ExpressionInterpreter makeInterpreter();

}
