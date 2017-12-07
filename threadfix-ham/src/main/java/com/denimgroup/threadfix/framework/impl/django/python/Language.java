package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class Language {
    public static Collection<String> PYTHON_KEYWORDS = list("if", "while", "else", "elif", "in", "do", "and", "not", "or", "import", "from", "\\");

    public static boolean isString(String value) {
        if (value.length() < 2) {
            return false;
        } else {
            int first = value.charAt(0);
            int second = value.charAt(1);
            if (first == '\'' || first == '"') {
                return true;
            } else if ((first == 'r' || first == 'u') && (second == '\'' || second == '"')) {
                return true;
            } else {
                return false;
            }
        }
    }


    public static boolean isNumber(String string) {
        for (int i = 0; i < string.length(); i++) {
            int c = string.charAt(i);
            if ((c < 48 || c > 57) && c != '.') {
                return false;
            }
        }
        return true;
    }
}
