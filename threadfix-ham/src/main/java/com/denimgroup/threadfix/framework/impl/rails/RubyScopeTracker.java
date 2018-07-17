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
