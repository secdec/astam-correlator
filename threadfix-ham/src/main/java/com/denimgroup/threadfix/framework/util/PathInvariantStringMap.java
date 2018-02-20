////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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
