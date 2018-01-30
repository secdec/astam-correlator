package com.denimgroup.threadfix.framework.util;

import java.util.HashMap;
import java.util.Map;

public class PathInvariantStringMap<Value> extends HashMap<String, Value> {

    @Override
    public Value get(Object key) {
        Value directValue = super.get(key);
        if (directValue != null) {
            return directValue;
        } else {
            Map.Entry<String, Value> entry = findPathEntry(key.toString());
            if (entry != null) {
                return entry.getValue();
            } else {
                return null;
            }
        }
    }

    @Override
    public Value put(String key, Value value) {
        Map.Entry<String, Value> existingEntry = findPathEntry(key);
        if (existingEntry != null) {
            Value result = existingEntry.getValue();
            existingEntry.setValue(value);
            return result;
        } else {
            return super.put(key, value);
        }
    }



    private Map.Entry<String, Value> findPathEntry(String path) {

        for (Map.Entry<String, Value> entry : this.entrySet()) {
            if (PathUtil.isEqualInvariant(path, entry.getKey())) {
                return entry;
            }
        }

        return null;
    }
}
