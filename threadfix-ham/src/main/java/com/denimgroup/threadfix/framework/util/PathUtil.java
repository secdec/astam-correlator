package com.denimgroup.threadfix.framework.util;

public class PathUtil {
    public static String combine(String begin, String end) {

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

        return result.toString();
    }
}
