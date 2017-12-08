package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonArray implements PythonValue {

    List<PythonValue> entries = list();
    AbstractPythonStatement sourceLocation = null;

    public void addEntry(PythonValue entry) {
        entries.add(entry);
    }

    public List<PythonValue> getEntries() {
        return entries;
    }

    public PythonValue entryAt(int index) {
        return entries.get(index);
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (!entries.contains(previousValue)) {
            return;
        }

        int index = entries.indexOf(previousValue);
        entries.remove(index);
        entries.add(index, newValue);
    }

    @Override
    public void resolveSourceLocation(AbstractPythonStatement source) {
        this.sourceLocation = source;
    }

    @Override
    public AbstractPythonStatement getSourceLocation() {
        return this.sourceLocation;
    }

    @Override
    public PythonValue clone() {
        PythonArray clone = new PythonArray();
        clone.sourceLocation = this.sourceLocation;
        cloneContentsTo(clone);
        return clone;
    }

    protected void cloneContentsTo(PythonArray array) {
        for (PythonValue entry : entries) {
            array.entries.add(entry.clone());
        }
    }

    @Override
    public List<PythonValue> getSubValues() {
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('[');

        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(entries.get(i).toString());
        }

        result.append(']');

        return result.toString();
    }
}
