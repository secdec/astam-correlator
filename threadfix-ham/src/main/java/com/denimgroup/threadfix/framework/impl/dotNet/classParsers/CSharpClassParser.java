package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerParser;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

public class CSharpClassParser extends AbstractCSharpParser<CSharpClass> implements EventBasedTokenizer {

    private CSharpAttributeParser attributeParser;
    private CSharpMethodParser methodParser;
    private CSharpScopeTracker scopeTracker;

    public static final SanitizedLogger LOG = new SanitizedLogger(DotNetControllerParser.class);

    @Override
    public void setParsingContext(CSharpParsingContext context) {
        attributeParser = context.getAttributeParser();
        methodParser = context.getMethodParser();
        scopeTracker = context.getScopeTracker();
    }

    @Override
    public void reset() {
        currentClassState = ClassState.SEARCH;
        possibleClassIsStatic = false;
        pendingClassName = null;
    }

    @Override
    public void resetAll() {
        reset();
        methodParser.reset();
        attributeParser.resetAll();
    }



    private enum ClassState {
        SEARCH, CLASS_NAME, TEMPLATE_PARAMS, BASE_TYPES, WHERE_CLAUSE, IN_CLASS, IN_INNER_CLASS
    }

    private ClassState currentClassState = ClassState.SEARCH;
    private boolean possibleClassIsStatic = false;
    private String pendingClassName = null;
    private String workingString = null;
    private int classBraceLevel = 1;

    @Override
    public void enableAll() {
        super.enableAll();
        attributeParser.enableAll();
        methodParser.enable();
    }

    @Override
    public void disableAll() {
        super.disableAll();
        methodParser.disable();
        attributeParser.disableAll();
    }

    private void prepareForInClass() {
        methodParser.enable();
        attributeParser.enableAll();
        classBraceLevel = scopeTracker.getNumOpenBrace();
        methodParser.setClassBraceLevel(classBraceLevel);
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (currentClassState != ClassState.SEARCH && currentClassState != ClassState.IN_CLASS) {
            attributeParser.disableAll();
        }

        CSharpClass pendingClass = getPendingItem();

        switch (currentClassState) {
            case SEARCH:
                if (attributeParser.isBuildingItem()) {
                    break;
                }

                if (type > 0) {
                    possibleClassIsStatic = false;
                } else if (CLASS.equals(stringValue)) {
                    pendingClass = new CSharpClass();
                    setPendingItem(pendingClass);

                    pendingClass.setIsStatic(possibleClassIsStatic);

                    while (attributeParser.hasItem()) {
                        pendingClass.addAttribute(attributeParser.pullCurrentItem());
                    }

                    possibleClassIsStatic = false;
                    pendingClassName = "";
                    currentClassState = ClassState.CLASS_NAME;
                }
                break;

            case CLASS_NAME:
                if (type == '{') {
                    if (pendingClass.getName() == null) {
                        pendingClass.setName(pendingClassName);
                    }
                    prepareForInClass();
                    currentClassState = ClassState.IN_CLASS;
                } else if (type == '<') {
                    pendingClass.setName(pendingClassName);
                    currentClassState = ClassState.TEMPLATE_PARAMS;
                } else if (":".equals(stringValue)) {
                    pendingClass.setName(pendingClassName);
                    currentClassState = ClassState.BASE_TYPES;
                } else if (type < 0 && "where".equals(stringValue) && !scopeTracker.isInString()) {
                    //  Template parameter type restrictions, ignore until we reach class definition
                    currentClassState = ClassState.WHERE_CLAUSE;
                } else if (type > 0) {
                    currentClassState = ClassState.SEARCH;
                } else {
                    pendingClassName += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;

            case BASE_TYPES:
                if (type == '{') {
                    if (workingString != null) {
                        pendingClass.addBaseType(workingString);
                        workingString = null;
                    }
                    currentClassState = ClassState.IN_CLASS;
                    prepareForInClass();
                } else if (type == ',' && scopeTracker.getNumOpenAngleBracket() == 0) {
                    pendingClass.addBaseType(workingString);
                    workingString = null;
                } else {
                    if (workingString == null) {
                        workingString = "";
                    }
                    workingString += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;

            case TEMPLATE_PARAMS:
                if (type == '>') {
                    if (workingString != null) {
                        pendingClass.addTemplateParameterName(workingString);
                        workingString = null;
                    }
                    currentClassState = ClassState.CLASS_NAME;
                } else if (type == ',' && workingString != null) {
                    pendingClass.addTemplateParameterName(workingString);
                    workingString = null;
                } else {
                    if (workingString == null) {
                        workingString = "";
                    }
                    workingString += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;

            case WHERE_CLAUSE:
                if (type == '{') {
                    currentClassState = ClassState.IN_CLASS;
                    prepareForInClass();
                }
                break;

            case IN_CLASS:
                while (methodParser.hasItem()) {
                    pendingClass.addMethod(methodParser.pullCurrentItem());
                }

                if (type == '}' && scopeTracker.getNumOpenBrace() < classBraceLevel) {
                    currentClassState = ClassState.SEARCH;
                    finalizePendingItem();
                    methodParser.disable();
                    resetAll();
                    break;
                }

                if (CLASS.equals(stringValue) && !scopeTracker.isInString()) {
                    currentClassState = ClassState.IN_INNER_CLASS;
                }

                break;

            case IN_INNER_CLASS:
                if (scopeTracker.getNumOpenBrace() <= classBraceLevel) {
                    currentClassState = ClassState.IN_CLASS;
                    prepareForInClass();
                }
                break;
        }
    }
}
