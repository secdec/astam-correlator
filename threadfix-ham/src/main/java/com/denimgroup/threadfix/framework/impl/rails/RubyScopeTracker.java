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

package com.denimgroup.threadfix.framework.impl.rails;

public class RubyScopeTracker {
    private int scopeDepth = 0;
    private int lastScopeDepth = 0;

    private boolean receivedLineValue = false;
    private int lastLineNumber = -1;

    private boolean escapeNext = false;
    private boolean isInString = false;
    private boolean isInComment = false;
    private boolean isMultilineComment = false;
    private int lastType = -1;

    public void accept(String stringValue, int type, int lineNumber) {

        if (lineNumber != lastLineNumber) {
            receivedLineValue = false;
            lastLineNumber = lineNumber;
            isInString = false;

            isInComment = isMultilineComment;
        }

        if (stringValue != null && type < 0 && !isInString && !isInComment) {
            if (!receivedLineValue && (
                    stringValue.equals("if") ||
                    stringValue.equals("unless")
                    )) {
                scopeDepth++;
            } else if (
                    stringValue.equals("while") ||
                    stringValue.equals("case") ||
                    stringValue.equals("def") ||
                    stringValue.equals("do") ||
                    stringValue.equals("class") ||
                    stringValue.equals("module") ||
                    stringValue.equals("begin")
                    ) {
                scopeDepth++;
            } else if (stringValue.equals("end")) {
                scopeDepth--;
            }
        }

        if (((type == '"' || type == '\'') && (lastType != '\'' || !escapeNext)) && stringValue == null) {
            isInString = !isInString;
            escapeNext = false;
        } else {
            escapeNext = type == '\\';
        }

        if (!isInString) {
            if (type == '#') {
                isInComment = true;
            } else if (stringValue != null && lastType == '=') {
                if (stringValue.equals("begin")) {
                    isInComment = true;
                    isMultilineComment = true;
                } else if (stringValue.equals("end")) {
                    isInComment = false;
                    isMultilineComment = false;
                }
            }
        }

        receivedLineValue = receivedLineValue || stringValue != null;

        lastType = type;

        assert scopeDepth >= 0;
    }

    public int getScopeDepth() {
        return scopeDepth;
    }

    public boolean enteredScope() {
        return scopeDepth > lastScopeDepth;
    }

    public boolean exitedScope() {
        return scopeDepth < lastScopeDepth;
    }

	public boolean isInComment() {
		return isInComment;
	}

	public boolean isInString() {
		return isInString;
	}
}
