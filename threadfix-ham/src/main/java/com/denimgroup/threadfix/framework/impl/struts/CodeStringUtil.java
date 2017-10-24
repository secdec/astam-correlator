package com.denimgroup.threadfix.framework.impl.struts;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CodeStringUtil {



    //  Splits the given code string at the top level by comma separator. This
    //      takes into account nested commas within strings, array lists, and parameter
    //      lists.
    public static String[] splitByComma(String codeLine) {
        int openBraceCnt = 0;
        int openParenCnt = 0;

        codeLine = codeLine.replace("\\([^\\])\\\"", "$1\"");
        if (codeLine.startsWith("{") || codeLine.startsWith("(")) {
            codeLine = codeLine.substring(1);
        }

        if (codeLine.endsWith("}") || codeLine.endsWith(")")) {
            codeLine = codeLine.substring(0, codeLine.length() - 1);
        }

        boolean isInInnerString = false;
        List<String> splitString = list();
        StringBuilder currentString = new StringBuilder();

        for (int i = 0; i < codeLine.length(); i++) {
            char c = codeLine.charAt(i);
            char p = 0;
            if (i > 0)
                p = codeLine.charAt(i - 1);

            if (c == '(')
                openParenCnt++;
            if (c == ')')
                openParenCnt--;
            if (c == '{')
                openBraceCnt++;
            if (c == '}')
                openBraceCnt--;

            if (c == ',' && openParenCnt == 0 && openBraceCnt == 0 && !isInInnerString) {
                String line = currentString.toString().replace("\n", "\\n");
                splitString.add(line);
                currentString = new StringBuilder();
            } else {
                currentString.append(c);
                if (c == '"' && p != '"') {
                    isInInnerString = !isInInnerString;
                }
            }
        }

        String lastString = currentString.toString();
        if (lastString.length() > 0) {
            splitString.add(lastString);
        }

        return splitString.toArray(new String[splitString.size()]);
    }
}
