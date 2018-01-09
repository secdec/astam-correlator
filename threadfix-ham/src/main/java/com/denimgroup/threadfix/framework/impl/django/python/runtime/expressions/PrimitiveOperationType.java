package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

public enum PrimitiveOperationType {
    ASSIGNMENT,           // '='
    CONCATENATION,        // '+='
    REMOVAL,              // '-='
    ADDITION,             // '+'
    SUBTRACTION,          // '-'
    STRING_INTERPOLATION, // '%'
    STRING_INTERPOLATION_ASSIGNMENT, // '%='
    UNKNOWN
}
