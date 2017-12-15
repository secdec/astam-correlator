package com.denimgroup.threadfix.framework.impl.django.python.runtime.pythonApi;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

public interface PythonApiProvider {
    boolean acceptsSubject(PythonValue subject);
    boolean acceptsSubjectOperand(PythonValue subject, PythonValue operand);
    AbstractPythonStatement resolveStatement(PythonValue subject, PythonValue operand, String statementSymbol);
}
