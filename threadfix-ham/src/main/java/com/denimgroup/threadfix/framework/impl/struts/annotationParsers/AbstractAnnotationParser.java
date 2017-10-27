package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.CodeParseUtil;
import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;

public abstract class AbstractAnnotationParser implements EventBasedTokenizer {

    static SanitizedLogger LOG = new SanitizedLogger(AbstractAnnotationParser.class.getName());

    private boolean isInClass = false;

//    protected String getCurrentClassName() {
//        return className;
//    }

    protected int openParenCount = 0;
    protected int openBraceCount = 0;
    protected int lastToken = -1;
    protected String lastString = null;

    public abstract Collection<Annotation> getAnnotations();

    protected abstract String getAnnotationName();
    protected abstract void onAnnotationFound(int type, int lineNumber, String stringValue);
    protected abstract void onParsingEnded(int type, int lineNumber, String stringValue);
    protected abstract void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType, int lineNumber);
    protected abstract void onAnnotationParameter(String value, int parameterIndex, int lineNumber);
    protected abstract void onNamedAnnotationParameter(String name, String value, int lineNumber);



    boolean isBuildInMethod(String methodName) {
        return
                methodName.equals("toString") ||
                methodName.equals("hashCode") ||
                methodName.equals("clone") ||
                methodName.equals("finalize") ||
                methodName.equals("equals");
    }


    String trimSpecialChars(String codeString) {
        return codeString
                .replaceAll("^[\",=\\s\\(\\)]+", "") // Start of string
                .replaceAll("[\",=\\s\\(\\)]+$", ""); // End of string
    }


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '{')
            openBraceCount++;
        if (type == '}')
            openBraceCount--;
        if (type == '(')
            openParenCount++;
        if (type == ')')
            openParenCount--;


        switch (parserPhase) {
            case IDENTIFICATION:
                processIdentificationPhase(type, lineNumber, stringValue);
                break;

            case PARSE_ANNOTATION:
                processParseAnnotationPhase(type, lineNumber, stringValue);
                break;

            case FIND_ATTACHMENT_TARGET:
                processFindAttachmentTargetPhase(type, lineNumber, stringValue);
                break;
        }

        if (stringValue != null)
            lastString = stringValue;
        if (type > 0)
            lastToken = type;
    }

    enum ParsePhase { IDENTIFICATION, PARSE_ANNOTATION, FIND_ATTACHMENT_TARGET }
    ParsePhase parserPhase = ParsePhase.IDENTIFICATION;

    void processIdentificationPhase(int type, int lineNumber, String stringValue) {
        if (type == '@') {
            parserPhase = ParsePhase.PARSE_ANNOTATION;

        }
    }

    int currentAnnotationParamIdx = -1;
    int annotationStartParenIdx = -1;
    boolean isInQuote = false;
    int paramValueStartBraceIndex = -1;
    String workingParamValue = null;
    String workingParamName = null;

    enum ParseAnnotationState { IDENTIFICATION, SKIP_FIRST_PAREN, START_NEW_PARAMETER, PARSE_PARAMETER }
    ParseAnnotationState parseAnnotationState = ParseAnnotationState.IDENTIFICATION;

    void processParseAnnotationPhase(int type, int lineNumber, String stringValue) {


        switch (parseAnnotationState) {
            case IDENTIFICATION:
                if (stringValue != null) {
                    if (!stringValue.equals(getAnnotationName())) {
                        parserPhase = ParsePhase.IDENTIFICATION;
                    } else {
                        currentAnnotationParamIdx = 0;
                        workingParamValue = "";
                        workingParamName = null;
                        parseAnnotationState = ParseAnnotationState.SKIP_FIRST_PAREN;
                        paramValueStartBraceIndex = openBraceCount;
                        onAnnotationFound(type, lineNumber, stringValue);
                    }
                }
                break;

            case PARSE_PARAMETER:
                boolean isEndOfAnnotation = type == ')' && annotationStartParenIdx > openParenCount;
                boolean isEndOfParam =
                        isEndOfAnnotation ||
                        (type == ',' && annotationStartParenIdx == openParenCount && !isInQuote && openBraceCount == paramValueStartBraceIndex);

                if (isEndOfParam) {
                    if (workingParamName != null) {
                        onNamedAnnotationParameter(workingParamName, workingParamValue, lineNumber);
                    } else {
                        onAnnotationParameter(workingParamValue, currentAnnotationParamIdx++, lineNumber);
                    }

                    workingParamValue = "";
                    workingParamName = null;
                }

                if (isEndOfAnnotation) {
                    onParsingEnded(type, lineNumber, stringValue);
                    parseAnnotationState = ParseAnnotationState.IDENTIFICATION;
                    parserPhase = ParsePhase.FIND_ATTACHMENT_TARGET;
                    return;
                }

                workingParamValue += CodeParseUtil.buildTokenString(type, stringValue);

                isInQuote = (type == '"' && lastToken != '\\' && stringValue == null);

                if (type == '=' && workingParamName == null) {
                    workingParamName = trimSpecialChars(workingParamValue);
                    workingParamValue = "";
                }
                break;

            case SKIP_FIRST_PAREN:
                parseAnnotationState = ParseAnnotationState.PARSE_PARAMETER;
                annotationStartParenIdx = openParenCount;
                break;
        }
    }

    enum FindAttachmentState { IDENTIFICATION, NEXT_IS_CLASS_NAME, VALIDATE_METHOD_DECL }
    FindAttachmentState findAttachmentState = FindAttachmentState.IDENTIFICATION;
    String currentClassName = null;
    String possibleMethodName = null;

    void processFindAttachmentTargetPhase(int type, int lineNumber, String stringValue) {
        switch (findAttachmentState) {
            case IDENTIFICATION:

                if (possibleMethodName != null && type == '(') {
                    onAnnotationTargetFound(possibleMethodName, Annotation.TargetType.METHOD, lineNumber);
                    possibleMethodName = null;
                    parserPhase = ParsePhase.IDENTIFICATION;
                } else if (stringValue == null) {
                    return;
                } else if (stringValue.equals("class")) {
                    findAttachmentState = FindAttachmentState.NEXT_IS_CLASS_NAME;
                } else {
                    possibleMethodName = stringValue;
                }

                break;

            case NEXT_IS_CLASS_NAME:
                currentClassName = stringValue;
                onAnnotationTargetFound(currentClassName, Annotation.TargetType.CLASS, lineNumber);
                parserPhase = ParsePhase.IDENTIFICATION;
                findAttachmentState = FindAttachmentState.IDENTIFICATION;
                break;
        }
    }
}
