package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.util.ScopeTracker;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class Language {
    public static Collection<String> PYTHON_KEYWORDS = list("if", "while", "else", "elif", "in", "do", "and", "not", "or", "import", "from", "with", "\\");

    public static boolean isString(String value) {
        if (value.length() < 2) {
            return false;
        } else {
            int first = value.charAt(0);
            int second = value.charAt(1);
            if (first != '\'' && first != '"' && !((first == 'r' || first == 'u') && (second == '\'' || second == '"'))) {
                return false;
            }

            ScopeTracker scopeTracker = new ScopeTracker();
            boolean startedString = false;
            for (int i = 0; i < value.length(); i++) {
                int c = value.charAt(i);
                scopeTracker.interpretToken(c);
                if (!startedString) {
                    if (scopeTracker.isInString()) {
                        startedString = true;
                    }
                } else {
                    if (!scopeTracker.isInString() && i != value.length() - 1) {
                        return false;
                    }
                }
            }

            return true;
        }
    }


    public static boolean isNumber(String string) {
        if (string.length() == 0) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            int c = string.charAt(i);
            if ((c < 48 || c > 57) && c != '.') {
                return false;
            }
        }
        return true;
    }

    public static String stripComments(String expression) {
        ScopeTracker scopeTracker = new ScopeTracker();

        for (int i = 0; i < expression.length(); i++) {
            int c = expression.charAt(i);
            scopeTracker.interpretToken(c);

            if (c == '#' && !scopeTracker.isInString()) {
                String result = expression.substring(0, i).trim();
                return result;
            }
        }

        return expression;
    }
}
