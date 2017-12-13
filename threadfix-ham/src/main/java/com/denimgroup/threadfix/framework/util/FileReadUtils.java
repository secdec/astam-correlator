package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class FileReadUtils {

    private static final SanitizedLogger LOG = new SanitizedLogger(FileReadUtils.class.getName());

    public static List<String> readLines(String filePath, int startLine, int endLine) {
        File file = new File(filePath);

        LOG.debug("Reading lines " + startLine + " through " + endLine + " from " + filePath + " as a collection of strings");

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            List<String> results = new ArrayList<String>(endLine - startLine + 1);
            for (int i = 0; i <= endLine; i++) {
                String line = reader.readLine();
                if (i >= startLine && line != null) {
                    results.add(line);
                }
            }

            LOG.debug("Read " + results.size() + " lines");
            return results;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readWholeLines(String filePath, int startLine, int endLine) {
        File file = new File(filePath);

        LOG.debug("Reading lines " + startLine + " through " + endLine + " from " + filePath + " as a whole string");

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            StringBuilder results = new StringBuilder();
            String line = reader.readLine();
            for (int i = 0; i <= endLine && line != null; i++) {
                if (i >= startLine) {
                    results.append(line);
                    results.append("\n");
                }

                line = reader.readLine();
            }
            return results.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return All lines in the file @filePath from @startLine to @endLine, where lines have been collapsed to a single line when a block or string spans multiple lines.
     */
    public static CondensedLinesMap readLinesCondensed(String filePath, int startLine, int endLine) {
        String contents = readWholeLines(filePath, startLine, endLine);
        if (contents == null) {
            return null;
        }

        LOG.debug("Reading lines " + startLine + " through " + endLine + " from " + filePath + " with lines condensed");

        StringReader stringReader = new StringReader(contents);
        BufferedReader reader = new BufferedReader(stringReader);

        ScopeTracker scopeTracker = new ScopeTracker();

        String currentLine = null;
        try {
            CondensedLinesMap result = new CondensedLinesMap();
            StringBuilder workingLine = new StringBuilder();
            int sourceLineNumber = 0;

            List<CondensedLineEntry> currentLineEntries = list();

            currentLine = reader.readLine();
            while (currentLine != null) {
                if (++sourceLineNumber < startLine) {
                    continue;
                }

                CondensedLineEntry newEntry = new CondensedLineEntry();
                newEntry.sourceLineNumber = sourceLineNumber;
                newEntry.text = currentLine;
                newEntry.condensedLineNumber = result.condensedLines.size() + 1;
                newEntry.condensedLineStartIndex = workingLine.length();

                for (int i = 0; i < currentLine.length(); i++) {
                    int c = currentLine.charAt(i);

                    //  Check for comment characters
                    if (!scopeTracker.isInString()) {
                        // Python-style comments
                        if (c == '#') {
                            break;
                            //  C/Java-style comments
                        } else if (workingLine.toString().endsWith("//")) {
                            break;
                        }
                    }

                    workingLine.append((char)c);
                    scopeTracker.interpretToken(c);
                }

                currentLineEntries.add(newEntry);

                if (!scopeTracker.isInScopeOrString() && !scopeTracker.isInString()) {
                    result.addCondensedLine(workingLine.toString(), currentLineEntries);
                    currentLineEntries.clear();
                    workingLine = new StringBuilder();
                } else {
                    workingLine.append(' ');
                }

                currentLine = reader.readLine();

                if (sourceLineNumber + 1 > endLine && !scopeTracker.isInString() && !scopeTracker.isInString()) {
                    break;
                }
            }

            if (workingLine.length() > 0) {
                result.addCondensedLine(workingLine.toString(), currentLineEntries);
            }

            LOG.debug("Finished reading condensed lines, condensed " + (sourceLineNumber - startLine) + " entries to " + result.lineEntries.size() + " lines");

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
