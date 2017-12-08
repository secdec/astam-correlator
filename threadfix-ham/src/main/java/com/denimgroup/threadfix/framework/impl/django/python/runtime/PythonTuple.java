package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public class PythonTuple extends PythonArray {

    @Override
    public PythonValue clone() {
        PythonTuple clone = new PythonTuple();
        clone.sourceLocation = this.sourceLocation;
        cloneContentsTo(clone);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('(');
        List<PythonValue> entries = this.getEntries();

        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(entries.get(i).toString());
        }

        result.append(')');

        return result.toString();
    }
}
