////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

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
