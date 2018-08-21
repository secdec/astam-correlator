package com.denimgroup.threadfix.framework.util;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class CaseInsensitiveStringMap<T> extends HashMap<String, T> {

    private Map<String, String> originalKeyMap = map();

    private String lowerCaseKey(Object key) {
        if (key == null || key.toString() == null) {
            return null;
        } else {
            return key.toString().toLowerCase();
        }
    }

    @Override
    public T get(Object key) {
        return super.get(lowerCaseKey(key));
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(lowerCaseKey(key));
    }

    @Override
    public T put(String key, T value) {
        String lowerCaseKey = lowerCaseKey(key);
        originalKeyMap.put(lowerCaseKey, key);
        return super.put(lowerCaseKey, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        for (Map.Entry<? extends String, ? extends T> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    @Nonnull
    public Set<String> keySet() {
        return originalKeyMap.keySet();
    }

    @Override
    @Nonnull
    public Set<Entry<String, T>> entrySet() {
        Set<Entry<String, T>> result = new HashSet<Entry<String, T>>();
        for (Map.Entry<String, T> entry : super.entrySet()) {
            String realKey = originalKeyMap.get(entry.getKey());
            result.add(new StringEntry(this, realKey, entry.getValue()));
        }
        return result;
    }




    private class StringEntry implements Map.Entry<String, T> {

        private CaseInsensitiveStringMap<T> ownerMap;
        private String key;
        private T value;

        public StringEntry(CaseInsensitiveStringMap<T> ownerMap, String key, T value) {
            this.ownerMap = ownerMap;
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public T setValue(T value) {
            this.value = this.ownerMap.put(this.key, value);
            return this.value;
        }
    }
}
