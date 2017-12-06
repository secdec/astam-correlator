package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;

import java.util.List;
import java.util.Map;

public class PythonObject implements PythonValue {

    String memberPath;
    PythonClass classType;

    public PythonObject() {

    }

    public PythonObject(String memberPath) {
        this.memberPath = memberPath;
    }

    public PythonObject(PythonClass classType) {
        this.classType = classType;
    }

    public PythonObject(PythonClass classType, String memberPath) {
        this.classType = classType;
        this.memberPath = memberPath;
    }

    public PythonClass getClassType() {
        return classType;
    }

    public String getMemberPath() {
        return memberPath;
    }

    public void setClassType(PythonClass classType) {
        this.classType = classType;
    }

    public void setMemberPath(String memberPath) {
        this.memberPath = memberPath;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }

    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (memberPath != null || classType != null) {
            result.append(memberPath);
            if (classType != null) {
                result.append(" (");
                result.append(classType.getName());
                result.append(')');
            }
        } else {
            result.append("<Unresolved Object>");
        }

        return result.toString();
    }
}
