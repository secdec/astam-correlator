package com.denimgroup.threadfix.framework.impl.django.python;

public class CondensedLineEntry {

    public String text;
    public int sourceLineNumber;

    public int condensedLineNumber;
    public int condensedLineStartIndex;


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(text);
        result.append(" - line ");
        result.append(sourceLineNumber);
        result.append(" in source, line ");
        result.append(condensedLineNumber);
        result.append(" in condensed");

        return result.toString();
    }
}
