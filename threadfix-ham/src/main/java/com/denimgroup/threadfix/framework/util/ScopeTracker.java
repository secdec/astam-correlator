package com.denimgroup.threadfix.framework.util;

public class ScopeTracker {

    int numOpenParen = 0, numOpenBrace = 0, numOpenBracket = 0;
    int stringStartToken = -1;
    boolean nextIsEscaped = false;

    public void interpretToken(int token) {
        if (token == '"' || token == '\'' && !nextIsEscaped) {
            if (stringStartToken < 0) {
                stringStartToken = token;
            } else if (token == stringStartToken) {
                stringStartToken = -1;
            }
        }

        nextIsEscaped = token == '\\' && !nextIsEscaped;

        if (!isInString()) {
            if (token == '(') numOpenParen++;
            if (token == ')') numOpenParen--;
            if (token == '{') numOpenBrace++;
            if (token == '}') numOpenBrace--;
            if (token == '[') numOpenBracket++;
            if (token == ']') numOpenBracket--;
        }
    }

    public boolean isInString() {
        return stringStartToken > 0;
    }

    public boolean isInScope() {
        return stringStartToken > 0 || numOpenParen > 0 || numOpenBrace > 0 || numOpenBracket > 0;
    }

    public int getNumOpenParen() {
        return numOpenParen;
    }

    public int getNumOpenBrace() {
        return numOpenBrace;
    }

    public int getNumOpenBracket() {
        return numOpenBracket;
    }
}
