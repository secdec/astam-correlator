package com.denimgroup.threadfix.framework.util;

import org.omg.CORBA.Environment;

import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileReadUtils {

    public static List<String> readLines(String filePath, int startLine, int endLine) {
        File file = new File(filePath);

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

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            StringBuilder results = new StringBuilder();
            for (int i = 0; i <= endLine; i++) {
                String line = reader.readLine();
                if (i >= startLine && line != null) {
                    if (results.length() == 0) {
                        results.append("\n");
                    }
                    results.append(line);
                }
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
    public static List<String> readLinesCondensed(String filePath, int startLine, int endLine) {
        String contents = readWholeLines(filePath, startLine, endLine);
        if (contents == null) {
            return null;
        }

        StringReader stringReader = new StringReader(contents);
        BufferedReader reader = new BufferedReader(stringReader);

        String currentLine = null;
        try {
            List<String> result = new ArrayList<String>(endLine - startLine + 1);
            StringBuilder workingLine = new StringBuilder();
            int numOpenParen = 0, numOpenBrace = 0, numOpenBracket = 0;
            int quoteStartType = -1;
            boolean escapeNextChar = false;

            currentLine = reader.readLine();
            while (currentLine != null) {
                for (int i = 0; i < currentLine.length(); i++) {
                    int c = currentLine.charAt(i);

                    if (c == '\'' || c == '"' && !escapeNextChar) {
                        if (quoteStartType > 0) {
                            if (c == quoteStartType) {
                                quoteStartType = -1;
                            }
                        } else {
                            quoteStartType = c;
                        }
                    }

                    escapeNextChar = (c == '\\' && !escapeNextChar);

                    if (quoteStartType < 0) {
                        if (c == '{') numOpenBrace++;
                        if (c == '}') numOpenBrace--;
                        if (c == '(') numOpenParen++;
                        if (c == ')') numOpenParen--;
                        if (c == '[') numOpenBracket++;
                        if (c == ']') numOpenBracket--;
                    }

                    workingLine.append((char)c);
                }

                if (quoteStartType < 0 && numOpenBrace == 0 && numOpenBracket == 0 && numOpenParen == 0) {
                    result.add(workingLine.toString());
                    workingLine = new StringBuilder();
                } else {
                    workingLine.append('\n');
                }

                currentLine = reader.readLine();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
