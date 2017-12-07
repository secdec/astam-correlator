package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.IndexerInterpreter;

import java.util.List;

public class IndexerExpression extends PythonUnaryExpression {

    PythonValue indexerValue = null;

    public void setIndexerValue(PythonValue indexerValue) {
        this.indexerValue = indexerValue;
    }

    public PythonValue getIndexerValue() {
        return indexerValue;
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {
        if (indexerValue != null) {
            targetList.add(indexerValue);
        }
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('(');
        for (int i = 0; i < numSubjects(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(getSubject(i).toString());
        }
        result.append(")[");
        if (indexerValue != null) {
            result.append(indexerValue.toString());
        } else {
            result.append("null");
        }
        result.append("]");

        return result.toString();
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new IndexerInterpreter();
    }
}
