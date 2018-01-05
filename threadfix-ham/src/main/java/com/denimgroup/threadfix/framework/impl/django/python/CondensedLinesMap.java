////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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


package com.denimgroup.threadfix.framework.impl.django.python;

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
