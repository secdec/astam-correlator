package com.denimgroup.threadfix.framework.util;

public class ScopeTracker {

    private int numOpenParen = 0, numOpenBrace = 0, numOpenBracket = 0;
    private int stringStartToken = -1;
    private boolean nextIsEscaped = false;
    private boolean enteredScope = false;
    private boolean exitedScope = false;

    private boolean enteredGlobalScope = false;
    private boolean exitedGlobalScope = false;

    private boolean enteredString = false;
    private boolean exitedString = false;

    public void interpretToken(int token) {

        enteredScope = false; exitedScope = false;
        enteredGlobalScope = false; exitedGlobalScope = false;
        enteredString = false; exitedString = false;

        if ((token == '"' || token == '\'') && !nextIsEscaped) {
            if (stringStartToken < 0) {
                stringStartToken = token;
                enteredString = true;
            } else if (token == stringStartToken) {
                stringStartToken = -1;
                exitedString = true;
            }
        }

        nextIsEscaped = token == '\\' && !nextIsEscaped;

        if (!isInString()) {
            boolean wasGlobalScope = !isInScopeOrString();
            boolean movedUpScope = false;
            boolean movedDownScope = false;
            if (token == '(') {
                numOpenParen++;
                movedUpScope = true;
            }
            if (token == ')') {
                numOpenParen--;
                movedDownScope = true;
            }
            if (token == '{') {
                numOpenBrace++;
                movedUpScope = true;
            }
            if (token == '}') {
                numOpenBrace--;
                movedDownScope = true;
            }
            if (token == '[') {
                numOpenBracket++;
                movedUpScope = true;
            }
            if (token == ']') {
                numOpenBracket--;
                movedDownScope = true;
            }

            if (movedUpScope) {
                enteredScope = true;
                if (wasGlobalScope) {
                    exitedGlobalScope = true;
                }
            }
            if (movedDownScope) {
                exitedScope = true;
                if (!isInScopeOrString()) {
                    enteredGlobalScope = true;
                }
            }
        }
    }

    public boolean isInString() {
        return stringStartToken > 0;
    }

    public boolean isInScope() {
        return numOpenParen > 0 || numOpenBrace > 0 || numOpenBracket > 0;
    }

    public boolean isInScopeOrString() {
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

    public boolean enteredScope() {
        return enteredScope;
    }

    public boolean exitedScope() {
        return exitedScope;
    }

    public boolean enteredGlobalScope() {
        return enteredGlobalScope;
    }

    public boolean exitedGlobalScope() {
        return exitedGlobalScope;
    }

    public boolean enteredString() {
        return enteredString;
    }

    public boolean exitedString() {
        return exitedString;
    }

    public boolean isNextEscaped() {
        return nextIsEscaped;
    }
}
