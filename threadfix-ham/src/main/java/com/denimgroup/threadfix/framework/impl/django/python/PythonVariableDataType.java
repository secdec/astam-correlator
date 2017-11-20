package com.denimgroup.threadfix.framework.impl.django.python;

public enum PythonVariableDataType {
    STRING_LITERAL, // Data is a Java String
    ARRAY, // Data is Collection<PythonValue>
    OBJECT, // Data is Java String
    FUNCTION // Data is Java String
}
