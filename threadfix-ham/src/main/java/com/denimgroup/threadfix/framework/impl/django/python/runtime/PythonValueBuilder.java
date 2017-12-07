package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import javax.annotation.Nonnull;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonValueBuilder {

    public <T extends PythonValue> T buildFromSymbol(@Nonnull String symbols, @Nonnull Class<T> type) {
        PythonValue value = buildFromSymbol(symbols);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T)value;
        } else {
            return null;
        }
    }

    public PythonValue buildFromSymbol(@Nonnull String symbols) {
        PythonValue result = null;
        ScopeTracker scopeTracker = new ScopeTracker();

        if ((symbols.startsWith("[") && symbols.endsWith("]")) || (symbols.startsWith("(") && symbols.endsWith(")"))) {

            PythonArray arrayResult;
            if (symbols.startsWith("[")) {
                arrayResult = new PythonArray();
            } else {
                arrayResult = new PythonTuple();
            }

            StringBuilder elementBuilder = new StringBuilder();

            symbols = CodeParseUtil.trim(symbols, new String[] { "[", "]", "(", ")" }, 1);

            for (int i = 0; i < symbols.length(); i++) {
                int token = symbols.charAt(i);
                scopeTracker.interpretToken(token);

                if (token == ',' && !scopeTracker.isInString() && !scopeTracker.isInScope()) {
                    PythonValue entry = new PythonUnresolvedValue(elementBuilder.toString().trim());
                    arrayResult.addEntry(entry);
                    elementBuilder = new StringBuilder();
                } else {
                    elementBuilder.append((char)token);
                }
            }

            if (elementBuilder.length() > 0) {
                arrayResult.addEntry(new PythonUnresolvedValue(elementBuilder.toString().trim()));
            }

            result = arrayResult;

        } else if (symbols.startsWith("{") && symbols.endsWith("}")) {

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

        } else if (Language.isString(symbols)) {
            result = new PythonStringPrimitive(CodeParseUtil.trim(symbols, new String[]{"\"", "'", "r'", "r\"", "u'", "u\""}));
        } else if (Language.isNumber(symbols)) {
            result = new PythonNumericPrimitive(symbols);
        } else if (symbols.equals("None")) {
            result = new PythonNone();
        } else {
            boolean isExpression = false;
            boolean isTuple = false;
            for (int i = 0; i < symbols.length(); i++) {
                int c = symbols.charAt(i);
                if ((c <= 64) || (c >= 91 && c <= 94) || (c == 96) || (c >= 123)) {
                    if (c == ',') {
                        isTuple = true;
                    } else if (c != '.') {
                        isExpression = true;
                    }
                }
            }

            if (isExpression) {
                result = new PythonIndeterminateValue();
            } else if (isTuple) {
                result = buildFromSymbol("(" + symbols + ")");
            } else {
                result = new PythonObject(symbols);
            }
        }

        return result;
    }

}
