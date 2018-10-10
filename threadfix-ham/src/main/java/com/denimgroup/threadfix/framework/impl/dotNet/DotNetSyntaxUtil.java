////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet;

import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.CS_KEYWORDS;

public class DotNetSyntaxUtil {

    public static boolean isAttributeSyntax(String string) {
        //  Could do a more rigorous test but this is sufficient
        return string.length() > 2 && string.charAt(0) == '[' && string.charAt(string.length() - 1) == ']';
    }

    public static boolean tokenIsValidInTypeName(char token) {
        return tokenIsValidInVariableName(token)
            || (token == '[' || token == ']')
            || (token == '<' || token == '>')
            || (token == '?')
            || (token == ',') // multiple template parameters in a type name
            || (token == '.') // For direct reference by namespace ie System.IO.File
        ;
    }

    public static boolean isValidVariableName(String string) {
        if (string == null) {
            return false;
        }

        for (int i = 0; i < string.length(); i++) {
            if (!tokenIsValidInVariableName(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean tokenIsValidInVariableName(char token) {
        return (token >= '0' && token <= '9')
            || (token >= 'A' && token <= 'Z')
            || (token >= 'a' && token <= 'z')
            || (token == '_');
    }

    public static boolean isValidTypeName(String string) {
        if (string == null || string.isEmpty())
            return false;

        if (CS_KEYWORDS.contains(string)) {
            return false;
        }

        int numOpenBracket = 0, numOpenTemplate = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!checkCharacterValidInContextualTypeName(c, string, i)) {
                return false;
            }

            switch (c) {
                case '<': numOpenTemplate++; break;
                case '>': numOpenTemplate--; break;
                case '[': numOpenBracket++; break;
                case ']': numOpenBracket--; break;
            }
        }

        return numOpenBracket == 0 && numOpenTemplate == 0;
    }

    public static boolean isValidPartialTypeName(String string) {
        if (CS_KEYWORDS.contains(string)) {
            return false;
        }

        int numOpenBracket = 0, numOpenTemplate = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!checkCharacterValidInContextualTypeName(c, string, i)) {
                return false;
            }

            switch (c) {
                case '<': numOpenTemplate++; break;
                case '>': numOpenTemplate--; break;
                case '[': numOpenBracket++; break;
                case ']': numOpenBracket--; break;
            }
        }

        return numOpenBracket >= 0 && numOpenTemplate >= 0;
    }

    private static boolean checkCharacterValidInContextualTypeName(char c, String string, int i) {
        if (!tokenIsValidInTypeName(c))
            return false;

        switch (c) {
            case '?':
                if (i == 0)
                    return false;
                break;

            case '.':
                if (i == 0 || i == string.length() - 1)
                    return false;
                break;

            case '<':
                if (i == 0 || i == string.length() - 1)
                    return false;
                break;

            case '>':
                if (i == 0)
                    return false;
                break;

            case '[':
                if (i == 0)
                    return false;
                if (i == string.length() - 1 || string.charAt(i + 1) != ']')
                    return false;
                break;

            case ']':
                // Starts with ']'
                if (i == 0)
                    return false;
                // Is not "[]"
                if (string.charAt(i - 1) != '[')
                    return false;
                break;
        }

        return true;
    }

    //  Strips template and array type syntax (or unwraps types in Task<>)
    public static String cleanTypeName(String typeName) {
        if (typeName.startsWith("Task") && typeName.length() > "Task".length()) {
            typeName = typeName.substring("Task<".length(), typeName.length() - 1);
        }

        StringBuilder cleanedName = new StringBuilder();
        for (int i = 0; i < typeName.length(); i++) {
            char c = typeName.charAt(i);
            if (!DotNetSyntaxUtil.tokenIsValidInVariableName(c)) {
                break;
            } else {
                cleanedName.append(c);
            }
        }
        return cleanedName.toString();
    }
}
