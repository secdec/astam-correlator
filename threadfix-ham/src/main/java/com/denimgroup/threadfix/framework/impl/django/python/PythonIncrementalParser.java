package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.PythonTokenizerConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

/**
 * Parses individual python code strings to generate a binary expression tree.
 */
public class PythonIncrementalParser implements EventBasedTokenizer {

    PythonCodeCollection linkedCodebase;
    AbstractPythonStatement statementContext;
    PythonBinaryExpression builtExpression;

    public static PythonBinaryExpression parse(PythonCodeCollection linkedCodebase, AbstractPythonStatement context, String pythonCodeLine) {
        PythonIncrementalParser parser = new PythonIncrementalParser(linkedCodebase, context);
        EventBasedTokenizerRunner.runString(pythonCodeLine, PythonTokenizerConfigurator.INSTANCE, parser);
        return parser.getBuiltExpression();
    }

    public PythonIncrementalParser(PythonCodeCollection codebase, AbstractPythonStatement context) {
        this.linkedCodebase = codebase;
        this.statementContext = context;
    }

    public PythonBinaryExpression getBuiltExpression() {
        return builtExpression;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    int numOpenBrace = 0, numOpenBracket = 0, numOpenParen = 0;
    int stringStartType = -1;
    boolean nextIsEscaped = false, wasEscaped = false;

    String lastString, lastValidString;
    int lastType, lastValidType;

    StringBuilder workingStringLiteral;
    StringBuilder pendingSubExpression;

    PythonValue targetObject = null;
    PythonValue operandObject = null;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '\'' || type == '"' && !nextIsEscaped) {
            if (stringStartType > 0) {
                if (type == stringStartType) {
                    stringStartType = -1;
                }
            } else {
                stringStartType = type;
                workingStringLiteral = new StringBuilder();
            }
        }

        if (stringStartType < 0) {
            if (type == '{') numOpenBrace++;
            if (type == '}') numOpenBrace--;
            if (type == '(') numOpenParen++;
            if (type == ')') numOpenParen--;
            if (type == '[') numOpenBracket++;
            if (type == ']') numOpenBracket--;
        }

        wasEscaped = nextIsEscaped;
        nextIsEscaped = type == '\\' && !nextIsEscaped;

        if (stringStartType > 0) {
            workingStringLiteral.append(CodeParseUtil.buildTokenString(type, stringValue));
        }

        PythonValue detectedValue = null;

        if (stringValue != null && stringStartType < 0) {
            if (workingStringLiteral != null) {
                String literalValue = workingStringLiteral.toString();
                literalValue = CodeParseUtil.trim(literalValue, new String[] { "'", "\"" });
                detectedValue = new PythonStringPrimitive(literalValue);
                workingStringLiteral = null;
            } else {
                detectedValue = new PythonObject();
            }
        } else {
            if (type > 0) {
                if (type == '[') {
                    detectedValue = new PythonArray();
                } else if (type == '{') {
                    detectedValue = new PythonDictionary();
                } else if (type == '(') {
                    detectedValue = new PythonParameterGroup();
                }
            }
        }

        if (detectedValue != null) {
            if (targetObject == null) {
                targetObject = detectedValue;
            } else if (operandObject == null) {
                operandObject = detectedValue;
            }
        }

        if (stringValue != null) lastValidString = stringValue;
        if (type > 0) lastValidType = type;

        lastString = stringValue;
        lastType = type;

    }




    enum ParsePhase { START }
}
