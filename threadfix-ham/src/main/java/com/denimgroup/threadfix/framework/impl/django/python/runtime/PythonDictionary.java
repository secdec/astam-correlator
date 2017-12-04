package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonDictionary implements PythonValue {

    Map<PythonValue, PythonValue> values = map();

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
}
