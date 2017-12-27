package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonArray implements PythonValue {

    List<PythonVariable> entries = list();
    AbstractPythonStatement sourceLocation = null;

    public void addEntry(PythonValue entry) {
        entries.add(makeVariable(entry));
    }

    public void setEntries(List<PythonValue> entries) {
        this.entries.clear();
        for (PythonValue value : entries) {
            this.entries.add(makeVariable(value));
        }
    }

    public void addEntries(Collection<PythonValue> entries) {
        for (PythonValue value : entries) {
            this.entries.add(makeVariable(value));
        }
    }

    public List<PythonVariable> getEntries() {
        return entries;
    }

    public List<PythonValue> getValues() {
        List<PythonValue> values = new ArrayList<PythonValue>(getEntries().size());
        for (PythonVariable entry : getEntries()) {
            values.add(entry.getValue());
        }
        return values;
    }

    public <T extends PythonValue> List<T> getValues(Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (PythonVariable entry : getEntries()) {
            PythonValue value = entry.getValue();
            if (value != null && type.isAssignableFrom(value.getClass())) {
                result.add((T)value);
            }
        }
        return result;
    }

    public PythonVariable entryAt(int index) {
        return entries.get(index);
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        int previousValueIndex = -1;
        PythonVariable existingVariable = null;
        List<PythonVariable> entries = getEntries();
        for (int i = 0; i < entries.size(); i++) {
            PythonVariable entry = entries.get(i);
            if ((entry.getValue() != null && entry.getValue() == previousValue) || entry == previousValue) {
                previousValueIndex = i;
                existingVariable = entry;
                break;
            }
        }

        if (previousValueIndex < 0) {
            return;
        } else {
            if (newValue instanceof PythonVariable) {
                newValue = ((PythonVariable) newValue).getValue();
            }

            existingVariable.setValue(newValue);
        }
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
        for (PythonVariable entry : entries) {
            array.entries.add((PythonVariable)entry.clone());
        }
    }

    public boolean testEquality(PythonArray other) {
        if (other.entries.size() != this.entries.size()) {
            return false;
        } else if (other.getClass() != this.getClass()) {
            return false;
        } else {
            for (int i = 0; i < entries.size(); i++) {
                PythonValue thisValue = this.entries.get(i);
                PythonValue otherValue = other.entries.get(i);

                if (!InterpreterUtil.testEquality(thisValue, otherValue)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public List<PythonValue> getSubValues() {
        return new ArrayList<PythonValue>(this.entries);
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

    private PythonVariable makeVariable(PythonValue value) {
        if (value instanceof PythonVariable) {
            PythonVariable result = (PythonVariable)value.clone();
            result.setLocalName(null);
            return result;
        } else {
            return new PythonVariable(null, value);
        }
    }
}
