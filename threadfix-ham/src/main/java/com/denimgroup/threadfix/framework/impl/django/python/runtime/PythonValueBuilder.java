package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

public class PythonValueBuilder {

    public PythonValue buildFromSymbol(String symbols) {
        PythonValue result = null;
        ScopeTracker scopeTracker = new ScopeTracker();

        if (symbols.startsWith("[") || symbols.startsWith("(")) {

            PythonArray arrayResult;
            if (symbols.startsWith("[")) {
                arrayResult = new PythonArray();
            } else {
                arrayResult = new PythonTuple();
            }

            StringBuilder elementBuilder = new StringBuilder();

            symbols = CodeParseUtil.trim(symbols, new String[] { "[", "]" }, 1);

            for (int i = 0; i < symbols.length(); i++) {
                int token = symbols.charAt(i);
                scopeTracker.interpretToken(token);

                if (token == ',' && !scopeTracker.isInString() && !scopeTracker.isInScope()) {
                    PythonUnresolvedValue entry = new PythonUnresolvedValue(elementBuilder.toString());
                    arrayResult.addEntry(entry);
                    elementBuilder = new StringBuilder();
                } else {
                    elementBuilder.append((char)token);
                }
            }

            if (elementBuilder.length() > 0) {
                arrayResult.addEntry(new PythonUnresolvedValue(elementBuilder.toString()));
            }

            result = arrayResult;

        } else if (symbols.startsWith("{")) {

            symbols = CodeParseUtil.trim(symbols, new String[] { "{", "}" }, 1);

            StringBuilder elementBuilder = new StringBuilder();
            String key = null;

            PythonDictionary dictionaryResult = new PythonDictionary();

            for (int i = 0; i < symbols.length(); i++) {
                int token = symbols.charAt(i);
                scopeTracker.interpretToken(token);

                if (!scopeTracker.isInScope() && !scopeTracker.isInString()) {
                    if (token == ':') {
                        if (key == null) {
                            key = elementBuilder.toString();
                            elementBuilder = new StringBuilder();
                        } else {
                            dictionaryResult.add(new PythonUnresolvedValue(key), new PythonUnresolvedValue(elementBuilder.toString()));
                            key = null;
                            elementBuilder = new StringBuilder();
                        }
                    }
                } else {
                    elementBuilder.append((char)token);
                }
            }

            if (elementBuilder.length() > 0 && key != null) {
                dictionaryResult.add(new PythonUnresolvedValue(key), new PythonUnresolvedValue(elementBuilder.toString()));
            }

            result = dictionaryResult;

        } else if (symbols.startsWith("\"") || symbols.startsWith("'") || symbols.startsWith("r'") || symbols.startsWith("r\"")) {
            result = new PythonStringPrimitive(CodeParseUtil.trim(symbols, new String[] { "\"", "'", "r'", "r\"" }));
        } else {
            result = new PythonIndeterminateValue();
        }

        return result;
    }

}
