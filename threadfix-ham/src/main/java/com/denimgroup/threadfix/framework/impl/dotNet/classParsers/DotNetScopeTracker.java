package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

public class DotNetScopeTracker implements EventBasedTokenizer {

    private ScopeTracker scopeTracker = new ScopeTracker();
    private int numOpenAngleBracket = 0;

    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        scopeTracker.interpretToken(type);
        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); i++) {
                char c = stringValue.charAt(i);
                scopeTracker.interpretToken((int)c);
            }

            //  A string value with 'type' specified means that the character 'type'
            //  surrounds the given string
            scopeTracker.interpretToken(type);
        }

        if (!isInString()) {
            switch (type) {
                case '<':
                    numOpenAngleBracket++;
                    break;
                case '>':
                    numOpenAngleBracket--;
                    break;
            }
        }
    }

    public int getStringStartToken() {
        return scopeTracker.getStringStartToken();
    }

    public boolean isInString() {
        return scopeTracker.isInString();
    }

    public boolean isInScope() {
        return scopeTracker.isInScope();
    }

    public int getNumOpenParen() {
        return scopeTracker.getNumOpenParen();
    }

    public int getNumOpenBrace() {
        return scopeTracker.getNumOpenBrace();
    }

    public int getNumOpenBracket() {
        return scopeTracker.getNumOpenBracket();
    }

    public int getNumOpenAngleBracket() { return numOpenAngleBracket; }

    public boolean enteredScope() {
        return scopeTracker.enteredScope();
    }

    public boolean exitedScope() {
        return scopeTracker.exitedScope();
    }

    public boolean enteredGlobalScope() {
        return scopeTracker.enteredGlobalScope();
    }

    public boolean enteredString() {
        return scopeTracker.enteredString();
    }

    public boolean exitedString() {
        return scopeTracker.exitedString();
    }

    public boolean isNextEscaped() {
        return scopeTracker.isNextEscaped();
    }
}
