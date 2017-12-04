package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonArray implements PythonValue {

    List<PythonValue> entries = list();

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
}
