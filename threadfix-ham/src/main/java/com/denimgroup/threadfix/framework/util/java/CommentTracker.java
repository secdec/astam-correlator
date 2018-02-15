package com.denimgroup.threadfix.framework.util.java;

public class CommentTracker {

    boolean isMultilineComment = false;
    boolean isLineComment = false;
    int lastToken = -1;

    public void interpretToken(int token) {
        if (token != '\n' && isLineComment) {
            return;
        }
        switch (token) {
            case '\n':
                isLineComment = false;
                break;
            case '*':
                if (lastToken == '/') {
                    isMultilineComment = true;
                }
                break;
            case '/':
                if (lastToken == '/') {
                    isLineComment = true;
                } else if (lastToken == '*') {
                    isMultilineComment = false;
                }
                break;
        }
        lastToken = token;
    }

    public boolean isInComment() {
        return isMultilineComment || isLineComment;
    }

}
