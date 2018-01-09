package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public class PythonSet extends PythonArray {

    @Override
    public PythonValue clone() {
        PythonSet clone = new PythonSet();
        this.cloneContentsTo(clone);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('{');
        List<PythonVariable> entries = this.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(entries.get(i).toString());
        }

        result.append('}');

        return result.toString();
    }
}
