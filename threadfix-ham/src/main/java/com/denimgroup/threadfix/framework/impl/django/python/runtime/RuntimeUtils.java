package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.Collection;

public class RuntimeUtils {

    public static boolean containsExpression(Collection<PythonValue> values) {
        return false;
//        for (PythonValue val : values) {
//            if (val instanceof PythonExpression) {
//                return true;
//            }
//        }
//        return false;
    }

}
