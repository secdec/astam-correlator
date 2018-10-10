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

package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.tokenIsValidInTypeName;

public class CSharpScopeTracker implements EventBasedTokenizer {

    private ScopeTracker scopeTracker;
    private int numOpenAngleBracket = 0;
    private int lastType = -1;
    private String lastString = null;
    private int lastLineNumber = -1;

    public CSharpScopeTracker() {
        scopeTracker = new ScopeTracker();
        scopeTracker.setInterpolationDetectorFactory(new CSharpInterpolationDetectorFactory());
        scopeTracker.setVerbatimStringDetector(new CSharpVerbatimStringDetector(scopeTracker));
    }


    private enum CommentState {
        SEARCH,
        SINGLE_LINE_COMMENT,
        MULTILINE_COMMENT,
        EXIT_MULTILINE_COMMENT
    }

    private CommentState currentCommentState = CommentState.SEARCH;


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        switch (currentCommentState) {
            case SEARCH:
                if (type > 0 && lastType > 0 && !scopeTracker.isInString()) {
                    if (type == '/' && lastType == '/') {
                        currentCommentState = CommentState.SINGLE_LINE_COMMENT;
                    } else if (type == '*' && lastType == '/') {
                        currentCommentState = CommentState.MULTILINE_COMMENT;
                    }
                }
                break;

            case SINGLE_LINE_COMMENT:
                if (lineNumber != lastLineNumber) {
                    currentCommentState = CommentState.SEARCH;
                }
                break;

            case MULTILINE_COMMENT:
                if (type > 0 && lastType > 0) {
                    if (type == '/' && lastType == '*') {
                        //  Need a delay to prevent later parsers from accepting the '/' from the end of the comment
                        currentCommentState = CommentState.EXIT_MULTILINE_COMMENT;
                    }
                }
                break;

            case EXIT_MULTILINE_COMMENT:
                currentCommentState = CommentState.SEARCH;
                break;
        }

        if (lastLineNumber != lineNumber) {
            lastLineNumber = lineNumber;
        }

        if (isInComment()) {
            lastType = type;
            return;
        }

        if (type > 0) {
            scopeTracker.interpretToken(type);
        }
        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); i++) {
                char c = stringValue.charAt(i);
                if (c == ' ') {
                    continue;
                }
                scopeTracker.interpretToken((int)c);
            }

            if (type > 0) {
                //  A string value with 'type' specified means that the character 'type'
                //  surrounds the given string
                scopeTracker.interpretToken(type);
            }
        }

        if (!isInString()) {
            if (type > 0 && !tokenIsValidInTypeName((char)type)) {
                numOpenAngleBracket = 0;
            }

            switch (type) {
                case '<':
                    numOpenAngleBracket++;
                    break;
                case '>':
                    if (numOpenAngleBracket > 0)
                        numOpenAngleBracket--;
                    break;
            }
        }

        lastType = type;
        lastString = stringValue;
    }

    public boolean isInComment() {
        return currentCommentState != CommentState.SEARCH;
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
