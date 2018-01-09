package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonDictionary implements PythonValue {

    Map<PythonValue, PythonVariable> values = map();
    AbstractPythonStatement sourceLocation;

    public void add(PythonValue key, PythonValue value) {
        //  Search for existing key
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (InterpreterUtil.testEquality(value, entry.getKey())) {
                entry.getValue().setValue(value);
                return;
            }
        }

        values.put(key, makeVariable(value));
    }

    public PythonValue get(PythonValue key) {
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (InterpreterUtil.testEquality(entry.getKey(), key)) {
                return entry.getValue();
            }
        }

        return new IndeterminateExpression();
    }

    public boolean testEquality(PythonDictionary other) {
        if (this.values.size() != other.values.size()) {
            return false;
        } else if (other.getClass() != this.getClass()) {
            return false;
        } else {
            for (Map.Entry<PythonValue, PythonVariable> entry : this.values.entrySet()) {
                PythonValue otherValue = other.get(entry.getKey());
                if (otherValue == null) {
                    return false;
                }
                if (!InterpreterUtil.testEquality(entry.getValue(), other)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (values.containsKey(previousValue)) {
            PythonVariable mapValue = values.get(previousValue);
            values.remove(previousValue);
            values.put(newValue, mapValue);
        } else if (values.containsValue(previousValue)) {
            PythonValue key = null;
            for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
                if (entry.getValue() == previousValue) {
                    key = entry.getKey();
                    break;
                }
            }

            if (key != null) {
                values.put(key, makeVariable(newValue));
            }
        }
    }

    @Override
    public void resolveSourceLocation(AbstractPythonStatement source) {
        sourceLocation = source;
    }

    @Override
    public AbstractPythonStatement getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public PythonValue clone() {
        PythonDictionary clone = new PythonDictionary();
        clone.sourceLocation = this.sourceLocation;
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            clone.values.put(
                    entry.getKey().clone(),
                    makeVariable(entry.getValue().clone())
            );
        }
        return clone;
    }

    @Override
    public List<PythonValue> getSubValues() {
        List<PythonValue> subValues = new ArrayList<PythonValue>(values.size() * 2);
        subValues.addAll(values.keySet());
        subValues.addAll(values.values());
        return subValues;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('{');

        int i = 0;
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (i++ > 0) {
                result.append(", ");
            }
            result.append(entry.getKey().toString());
            result.append(": ");
            result.append(entry.getValue().toString());
        }

        result.append('}');

        return result.toString();
    }

    private PythonVariable makeVariable(PythonValue value) {
        if (value instanceof PythonVariable) {
            return (PythonVariable)value.clone();
        } else {
            return new PythonVariable(null, value);
        }
    }
}
