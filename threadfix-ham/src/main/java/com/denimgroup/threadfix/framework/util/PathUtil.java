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

    //  Compares two paths, ignoring capitalization and directory '/' formatting
    public static boolean isEqualInvariant(String a, String b) {
        a = trimAll(a, "/");
        a = trimAll(a, "\\");
        b = trimAll(b, "/");
        b = trimAll(b, "\\");

        return a.equalsIgnoreCase(b);
    }

    private static String trimAll(String text, String unwantedText) {
        while (text.startsWith(unwantedText)) {
            text = text.substring(unwantedText.length());
        }

        while (text.endsWith(unwantedText)) {
            text = text.substring(0, text.length() - unwantedText.length());
        }

        return text;
    }
}
