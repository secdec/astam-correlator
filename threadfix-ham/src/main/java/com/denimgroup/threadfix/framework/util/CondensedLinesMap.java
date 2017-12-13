package com.denimgroup.threadfix.framework.util;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CondensedLinesMap {

    List<CondensedLineEntry> lineEntries = list();
    List<String> condensedLines = list();

    public void addEntry(CondensedLineEntry entry) {
        lineEntries.add(entry);
    }

    public void addCondensedLine(String lineText, Collection<CondensedLineEntry> entries) {
        condensedLines.add(lineText);
        lineEntries.addAll(entries);
    }

    public List<String> getCondensedLines() {
        return condensedLines;
    }

    public List<CondensedLineEntry> getLineEntries() {
        return lineEntries;
    }

    public int getLineIndexForSourceLine(int sourceLine) {
        int line = -1;
        for (CondensedLineEntry entry : lineEntries) {
            if (entry.sourceLineNumber == sourceLine) {
                line = entry.condensedLineNumber;
                break;
            }
        }
        return line;
    }

    public List<CondensedLineEntry> getEntriesForCondensedLine(int lineNumber) {
        List<CondensedLineEntry> result = list();
        for (CondensedLineEntry entry : lineEntries) {
            if (entry.condensedLineNumber == lineNumber) {
                result.add(entry);
            } else if (entry.condensedLineNumber > lineNumber) {
                break;
            }
        }
        return result;
    }

    public int getLineForCondensedEntry(int condensedLineNumber, int characterIndex) {
        for (CondensedLineEntry entry : lineEntries) {
            if (entry.condensedLineNumber == condensedLineNumber &&
                    entry.condensedLineStartIndex >= characterIndex &&
                    entry.condensedLineStartIndex + entry.text.length() <= characterIndex) {
                return entry.sourceLineNumber;
            } else if (entry.condensedLineNumber > condensedLineNumber) {
                break;
            }
        }
        return -1;
    }

}
