package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.PythonTokenizerConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

/**
 * Parses individual python code strings to generate a binary expression tree.
 */
public class PythonIncrementalParser implements EventBasedTokenizer {

    PythonCodeCollection linkedCodebase;
    AbstractPythonStatement statementContext;
    PythonBinaryExpression builtExpression;

    public static PythonBinaryExpression parse(PythonCodeCollection linkedCodebase, AbstractPythonStatement context, String pythonCodeLine) {
        PythonIncrementalParser parser = new PythonIncrementalParser(linkedCodebase, context);
        EventBasedTokenizerRunner.runString(pythonCodeLine, PythonIncrementalParserTokenizerConfigurator.INSTANCE, parser);
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

    String lastString, lastValidString;
    int lastType, lastValidType;
    boolean endedToken = false;

    StringBuilder workingLine = new StringBuilder();
    StringBuilder pendingSubExpression;

    ScopeTracker scopeTracker = new ScopeTracker();

    PythonValue targetObject = null;
    PythonValue operandObject = null;

    int primitiveOperator1 = -1, primitiveOperator2 = -1;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        scopeTracker.interpretToken(type);
        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); i++) {
                scopeTracker.interpretToken(stringValue.charAt(i));
            }
            scopeTracker.interpretToken(type);
        }
        workingLine.append(CodeParseUtil.buildTokenString(type, stringValue));

        int detectedOperator = -1;
        if (type == '=' || type == '+' || type == '-') {
            detectedOperator = type;
        }

        if (detectedOperator > 0) {
            if (primitiveOperator1 < 0) {
                primitiveOperator1 = detectedOperator;
            } else {
                primitiveOperator2 = detectedOperator;
            }
        }

        if (workingLine.length() > 0) {
            if (!scopeTracker.isInString() && !scopeTracker.isInScope()) {
                if (type == '.') {
                    endedToken = true;
                }
            }
        }

        if (stringValue != null) lastValidString = stringValue;
        if (type > 0) lastValidType = type;

        lastString = stringValue;
        lastType = type;

    }




    enum ParsePhase { START }
}
