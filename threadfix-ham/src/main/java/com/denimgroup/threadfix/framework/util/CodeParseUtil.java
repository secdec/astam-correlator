package com.denimgroup.threadfix.framework.util;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CodeParseUtil {

    public static String trim(String string, String[] tokens, int maxDepth) {
        string = string.trim();
        boolean needsTrimStart = true;
        for (int i = 0; i < maxDepth && needsTrimStart; i++) {
            needsTrimStart = false;
            for (String token : tokens) {
                if (string.startsWith(token)) {
                    string = string.substring(token.length()).trim();
                    needsTrimStart = true;
                }
            }
        }

        boolean needsTrimEnd = true;
        for (int i = 0; i < maxDepth && needsTrimEnd; i++) {
            needsTrimEnd = false;
            for (String token : tokens) {
                if (string.endsWith(token)) {
                    string = string.substring(0, string.length() - token.length()).trim();
                    needsTrimEnd = true;
                }
            }
        }

        return string;
    }

    public static String trim(String string, String[] tokens) {
        return trim(string, tokens, 10);
    }

    //  Converts the given character type and string value to its original string representation.
    public static String buildTokenString(int type, String stringValue) {
        StringBuilder result = new StringBuilder();

        //  Add character as prefix
        if (type > 0) {
            result.append((char)type);
        }

        //  Add string token if available
        if (stringValue != null) {
            result.append(stringValue);

            //  If 'stringValue' and 'type' are both assigned, the string should be 'stringValue' surrounded
            //  by 'type'. For example: stringValue="foo",type='&' results in "&foo&".
            if (type > 0) {
                result.append((char)type);
            }
        }

        return result.toString();
    }

    public static String[] splitByComma(String codeLine) {
        return splitByComma(codeLine, true);
    }

    //  Splits the given code string at the top level by comma separator. This
    //      takes into account nested commas within strings, array lists, and parameter
    //      lists.
    public static String[] splitByComma(String codeLine, boolean trimEnds) {
        codeLine = codeLine.replace("\\([^\\])\\\"", "$1\"");
        if (trimEnds) {
            if (codeLine.startsWith("{") || codeLine.startsWith("(") || codeLine.startsWith("[")) {
                codeLine = codeLine.substring(1);
            }

            if (codeLine.endsWith("}") || codeLine.endsWith(")") || codeLine.endsWith("]")) {
                codeLine = codeLine.substring(0, codeLine.length() - 1);
            }
        }

        ScopeTracker scopeTracker = new ScopeTracker();
        List<String> splitString = list();
        StringBuilder currentString = new StringBuilder();

        for (int i = 0; i < codeLine.length(); i++) {
            char c = codeLine.charAt(i);
            scopeTracker.interpretToken((int)c);
            char p = 0;
            if (i > 0)
                p = codeLine.charAt(i - 1);

            if (c == ',' && !scopeTracker.isInString() && !scopeTracker.isInScopeOrString()) {
                String line = currentString.toString().trim().replace("\n", "\\n");
                splitString.add(line);
                currentString = new StringBuilder();
            } else {
                currentString.append(c);
            }
        }

        String lastString = currentString.toString();
        if (lastString.length() > 0) {
            splitString.add(lastString.trim());
        }

        return splitString.toArray(new String[splitString.size()]);
    }
}
