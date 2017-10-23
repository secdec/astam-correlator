package com.denimgroup.threadfix.framework.impl.struts.annotationParsers;

import com.denimgroup.threadfix.framework.impl.struts.model.annotations.Annotation;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.Collection;

public abstract class AbstractAnnotationParser implements EventBasedTokenizer {

    private boolean isInClass = false;
    private boolean hasFoundAnnotation = false;
    private boolean isParsingAnnotation = false;
    private String lastToken = null;
    private int lastType = -1;
    private int startParenIndex = -1;
    private boolean nextIsClassAnnotationTarget = false;
    private String possibleCurrentAnnotationTarget = null;
    private boolean isValidatingMethodCall = false;
    private int currentParameterIndex = -1;
    private int methodStartBraceIndex = -1;
    private int annotationParameterStartParenIndex = -1;
    private int annotationParameterStartBraceIndex = -1;
    private String workingAnnotationParameterValue = null;
    private String workingAnnotationParameterName = null;
    private boolean isInMethod = false;
    private String className = null;
    private boolean isPublicMember = false;
    private boolean isFindingAnnotationTarget = false;

    protected String getCurrentClassName() {
        return className;
    }

    protected int openParenCount = 0;
    protected int openBraceCount = 0;

    public abstract Collection<Annotation> getAnnotations();

    protected abstract String getAnnotationName();
    protected abstract void onAnnotationFound(int type, int lineNumber, String stringValue);
    protected abstract void onParsingEnded(int type, int lineNumber, String stringValue);
    protected abstract void onAnnotationTargetFound(String targetName, Annotation.TargetType targetType);
    protected abstract void onAnnotationParameter(String value, int parameterIndex);
    protected abstract void onNamedAnnotationParameter(String name, String value);


    //  TODO - Move this implementation to a state machine


    protected void processAnnotationToken(int type, int lineNumber, String stringValue) {

    }

    boolean isAccessor(String methodName) {
        return methodName.startsWith("set") || methodName.startsWith("get");
    }

    boolean isBuiltInMethod(String methodName) {
        return methodName.equals("toString") ||
                methodName.equals("hashCode") ||
                methodName.equals("equals") ||
                methodName.equals("clone") ||
                methodName.equals("finalize");
    }

    String trimParamNameSpecialChars(String text) {
        if (text.startsWith("(") || text.startsWith("\"") || text.startsWith("=")) {
            text = text.substring(1);
        }
        if (text.endsWith(")") || text.endsWith("\"") || text.endsWith("=")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }


    @Override
    public final void processToken(int type, int lineNumber, String stringValue) {

        String typeAsString = Character.toString((char)type);

        if (type == '(') {
            openParenCount++;
        } else if (type == ')') {
            openParenCount--;
        }

        if (type == '{') {
            openBraceCount++;
        } else if (type == '}') {
            openBraceCount--;
        }

        if (!isParsingAnnotation) {
            if (type == '{' && className != null) {
                isInClass = true;
            }

            if (type == '}' && methodStartBraceIndex == openBraceCount) {
                isInMethod = false;
            }
        } else {

            //  If stringValue is wrapper in some token ie ", then both stringValue and its wrapper
            //      token will be specified
            //  Apply wrapper token twice if stringValue is specified

            if (type > 0) {
                workingAnnotationParameterValue += Character.toString((char)type);
            }

            if (stringValue != null) {
                workingAnnotationParameterValue += stringValue;

                if (type > 0) {
                    workingAnnotationParameterValue += Character.toString((char)type);
                }
            }

            if (type > 0) {
                if (type == ',' && openParenCount <= annotationParameterStartParenIndex + 1 && openBraceCount == annotationParameterStartBraceIndex) {

                    if (workingAnnotationParameterName != null) {
                        onNamedAnnotationParameter(trimParamNameSpecialChars(workingAnnotationParameterName), workingAnnotationParameterValue);
                    } else {
                        onAnnotationParameter(workingAnnotationParameterValue, currentParameterIndex);
                    }

                    workingAnnotationParameterName = null;
                    workingAnnotationParameterValue = "";

                } else if (type == ')' && openParenCount <= annotationParameterStartParenIndex + 1) {

                    if (workingAnnotationParameterName != null) {
                        workingAnnotationParameterName = trimParamNameSpecialChars(workingAnnotationParameterName);
                    }

                    if (workingAnnotationParameterName != null) {
                        onNamedAnnotationParameter(workingAnnotationParameterName, workingAnnotationParameterValue);
                    } else {
                        onAnnotationParameter(workingAnnotationParameterValue, currentParameterIndex);
                    }

                    annotationParameterStartParenIndex = -1;
                    annotationParameterStartBraceIndex = -1;

                    workingAnnotationParameterValue = null;
                    workingAnnotationParameterName = null;

                } else if (type == '=' && openBraceCount == annotationParameterStartBraceIndex && openParenCount <= annotationParameterStartParenIndex + 1) {

                    workingAnnotationParameterName = workingAnnotationParameterValue;
                    workingAnnotationParameterValue = "";

                }
            }
        }

        if (type == ')' && startParenIndex == openParenCount && isParsingAnnotation) {
            onParsingEnded(type, lineNumber, stringValue);
            hasFoundAnnotation = false;
            isParsingAnnotation = false;
        }

        if (hasFoundAnnotation) {
            if (stringValue.equalsIgnoreCase(getAnnotationName())) {
                isParsingAnnotation = true;
                startParenIndex = openParenCount;
                workingAnnotationParameterValue = "";
                annotationParameterStartParenIndex = openParenCount;
                annotationParameterStartBraceIndex = openBraceCount;
                currentParameterIndex = 0;
                onAnnotationFound(type, lineNumber, stringValue);
                isFindingAnnotationTarget = true;
            }
            hasFoundAnnotation = false;
        } if (isParsingAnnotation) {
            processAnnotationToken(type, lineNumber, stringValue);
        } else if (type == '@') {
            hasFoundAnnotation = true;
        }

        if (nextIsClassAnnotationTarget) {
            if (isFindingAnnotationTarget) {
                onAnnotationTargetFound(stringValue, Annotation.TargetType.CLASS);
            }
            isFindingAnnotationTarget = false;
            nextIsClassAnnotationTarget = false;
            className = stringValue;
        }

        if (isInClass && stringValue != null && stringValue.equals("public")) {
            isPublicMember = true;
        }

        if (isValidatingMethodCall) {
            if (lastType == ')' && type > 0) {
                if (type == '{' && isFindingAnnotationTarget) {
                    if (!isAccessor(possibleCurrentAnnotationTarget) && !possibleCurrentAnnotationTarget.equals(className) && !isBuiltInMethod(possibleCurrentAnnotationTarget) && isPublicMember) {
                        onAnnotationTargetFound(possibleCurrentAnnotationTarget, Annotation.TargetType.METHOD);
                    }
                    isFindingAnnotationTarget = false;
                    isInMethod = true;
                    methodStartBraceIndex = openBraceCount;
                }

                possibleCurrentAnnotationTarget = null;
                isValidatingMethodCall = false;
                isPublicMember = false;
            }
        }

        if (!isParsingAnnotation && !isValidatingMethodCall && !isInMethod) {
            if (stringValue != null && stringValue.equals("class")) {
                nextIsClassAnnotationTarget = true;
            } else if (type == '(' && possibleCurrentAnnotationTarget == null) {
                possibleCurrentAnnotationTarget = lastToken;
                isValidatingMethodCall = true;
            }
        }

        processAnnotationToken(type, lineNumber, stringValue);

        if (stringValue != null)
            lastToken = stringValue;
        if (type > 0)
            lastType = type;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    private class ParameterValue
    {
        public String name;
        public String value;
    }
}
