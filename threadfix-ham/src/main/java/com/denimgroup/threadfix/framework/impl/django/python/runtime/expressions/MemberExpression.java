package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.MemberInterpreter;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class MemberExpression extends PythonUnaryExpression {

    List<String> memberPath = list();

    public void appendPath(String relativePath) {
        this.memberPath.add(relativePath);
    }

    public void removePath(int index) {
        this.memberPath.remove(index);
    }

    public List<String> getMemberPath() {
        return memberPath;
    }

    public String getFullMemberPath() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < memberPath.size(); i++) {
            String part = memberPath.get(i);
            if (i > 0) {
                sb.append('.');
            }
            sb.append(part);
        }
        return sb.toString();
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        replaceSubject(previousValue, newValue);
    }

    @Override
    public PythonValue clone() {
        MemberExpression clone = new MemberExpression();
        clone.resolveSourceLocation(this.getSourceLocation());
        clone.memberPath.addAll(this.memberPath);
        cloneSubjectsTo(clone);
        return clone;
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (this.numSubjects() > 0) {
            result.append('(');
            for (int i = 0; i < this.numSubjects(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.getSubject(i).toString());
            }
            result.append(")");
        }

        result.append('.');
        result.append(getFullMemberPath());

        return result.toString();
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new MemberInterpreter();
    }
}
