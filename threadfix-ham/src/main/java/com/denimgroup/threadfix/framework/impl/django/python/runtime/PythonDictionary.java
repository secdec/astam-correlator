package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonDictionary implements PythonValue {

    Map<PythonValue, PythonValue> values = map();
    AbstractPythonStatement sourceLocation;

    public void add(PythonValue key, PythonValue value) {
        values.put(key, value);
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (values.containsKey(previousValue)) {
            PythonValue mapValue = values.get(previousValue);
            values.remove(previousValue);
            values.put(newValue, mapValue);
        } else if (values.containsValue(previousValue)) {
            PythonValue key = null;
            for (Map.Entry<PythonValue, PythonValue> entry : values.entrySet()) {
                if (entry.getValue() == previousValue) {
                    key = entry.getKey();
                    break;
                }
            }

            if (key != null) {
                values.put(key, newValue);
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
        for (Map.Entry<PythonValue, PythonValue> entry : values.entrySet()) {
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
}
