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
            || (token == '.') // For direct reference by namespace ie System.IO.File
        ;
    }

    public static boolean isValidVariableName(String string) {
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
}
