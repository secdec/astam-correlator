package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import javax.annotation.Nonnull;

public class PythonValueBuilder {

    private enum ValueType {
        ARRAY,
        SET,
        TUPLE,
        IMPLICIT_TUPLE,
        MAP,
        STRING,
        NUMBER,
        NONE,
        UNKNOWN
    }

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

        ValueType valueType = determineValueType(symbols);
        StringBuilder elementBuilder = new StringBuilder();

        switch (valueType) {
            case TUPLE:
            case IMPLICIT_TUPLE:
            case SET:
            case ARRAY:
                PythonArray arrayResult;
                if (valueType == ValueType.ARRAY) {
                    arrayResult = new PythonArray();
                    symbols = CodeParseUtil.trim(symbols, new String[] { "[", "]" }, 1);
                } else if (valueType == ValueType.TUPLE) {
                    arrayResult = new PythonTuple();
                    symbols = CodeParseUtil.trim(symbols, new String[] { "(", ")" }, 1);
                } else if (valueType == ValueType.SET) {
                    arrayResult = new PythonSet();
                    symbols = CodeParseUtil.trim(symbols, new String[] { "{", "}" });
                } else {
                    // IMPLICIT_TUPLE
                    arrayResult = new PythonTuple();
                }

                for (int i = 0; i < symbols.length(); i++) {
                    int token = symbols.charAt(i);
                    scopeTracker.interpretToken(token);

                    if (token == ',' && !scopeTracker.isInString() && !scopeTracker.isInScopeOrString()) {
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
                break;
            case MAP:
                symbols = CodeParseUtil.trim(symbols, new String[] { "{", "}" }, 1);
                String key = null;

                PythonDictionary dictionaryResult = new PythonDictionary();

                for (int i = 0; i < symbols.length(); i++) {
                    int token = symbols.charAt(i);
                    scopeTracker.interpretToken(token);

                    if (!scopeTracker.isInScopeOrString() && !scopeTracker.isInString()) {
                        if (token == ':') {
                            if (key == null) {
                                key = elementBuilder.toString();
                                elementBuilder = new StringBuilder();
                            }
                        } else if (token == ',') {
                            dictionaryResult.add(new PythonUnresolvedValue(key), new PythonUnresolvedValue(elementBuilder.toString()));
                            key = null;
                            elementBuilder = new StringBuilder();
                        } else {
                            elementBuilder.append((char)token);
                        }
                    } else {
                        elementBuilder.append((char)token);
                    }
                }

                if (elementBuilder.length() > 0 && key != null) {
                    dictionaryResult.add(new PythonUnresolvedValue(key), new PythonUnresolvedValue(elementBuilder.toString()));
                }

                result = dictionaryResult;
                break;

            case STRING:
                result = new PythonStringPrimitive(CodeParseUtil.trim(symbols, new String[]{"\"", "'", "r'", "r\"", "u'", "u\""}));
                break;

            case NUMBER:
                result = new PythonNumericPrimitive(symbols);
                break;

            case NONE:
                result = new PythonNone();
                break;

            default:
                boolean isExpression = false;
                boolean isTuple = false;
                for (int i = 0; i < symbols.length(); i++) {
                    int c = symbols.charAt(i);
                    if ((c < 48) || (c >= 58 && c <= 64) || (c >= 91 && c <= 94) || (c >= 123)) {
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
                    result = new PythonVariable(symbols);
                }
                break;

        }

        return result;
    }

    private ValueType determineValueType(String symbols) {
        ValueType possibleType = ValueType.UNKNOWN;
        ScopeTracker scopeTracker = new ScopeTracker();
        if (symbols.equals("None")) {
            possibleType = ValueType.NONE;
        } else if (Language.isString(symbols)) {
            possibleType = ValueType.STRING;
        } else if (Language.isNumber(symbols)) {
            possibleType = ValueType.NUMBER;
        } else {
            for (int i = 0; i < symbols.length(); i++) {
                char c = symbols.charAt(i);
                scopeTracker.interpretToken(c);

                if (i == 0) {
                    switch (c) {
                        case '[':
                            possibleType = ValueType.ARRAY;
                            break;
                        case '(':
                            possibleType = ValueType.TUPLE;
                            break;
                        case '{':
                            possibleType = ValueType.SET;
                            break;
                    }
                } else {
                    switch (possibleType) {
                        case ARRAY:
                            if (scopeTracker.getNumOpenBracket() == 0 && i != symbols.length() - 1) {
                                possibleType = ValueType.UNKNOWN;
                            }
                            break;
                        case TUPLE:
                            if (scopeTracker.getNumOpenParen() == 0 && i != symbols.length() - 1) {
                                possibleType = ValueType.UNKNOWN;
                            }
                            break;
                        case SET:
                            if (scopeTracker.getNumOpenBrace() == 0 && i != symbols.length() - 1) {
                                possibleType = ValueType.UNKNOWN;
                            } else if (!scopeTracker.isInString() && c == ':') {
                                possibleType = ValueType.MAP;
                            }
                            break;
                    }
                }
            }
        }

//        if (possibleType == ValueType.UNKNOWN) {
//            String[] parts = CodeParseUtil.splitByComma(symbols);
//            if (parts.length > 1) {
//                //  It may be an implicit tuple, make sure the contents
//            }
//        }

        return possibleType;
    }

}
