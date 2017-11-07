package com.denimgroup.threadfix.framework.util;

public class PathUtil {

    public static String combine(String begin, String end) {
        return combine(begin, end, true);
    }

    public static String combine(String begin, String end, boolean prefixWithSlash) {

        if (begin == null) {
            begin = "";
        }
        if (end == null) {
            end = "";
        }

        if (begin.endsWith("/")) {
            begin = begin.substring(0, begin.length() - 1);
        }

        StringBuilder result = new StringBuilder();
        result.append(begin);

        if (!end.startsWith("/")) {
            result.append('/');
        }

        result.append(end);
        String string = result.toString();
        //  TODO - Messy, clean this up
        if (prefixWithSlash) {
            if (!string.startsWith("/")) {
                string = "/" + string;
            }
        } else {
            if (string.startsWith("/")) {
                string = string.substring(1);
            }
        }

        return string;
    }
}
