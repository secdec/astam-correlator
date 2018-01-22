package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPElementReferenceParser implements EventBasedTokenizer {

    public JSPElementReferenceParser() {

    }

    public JSPElementReferenceParser(List<String> watchedElementTypes) {
        watchedElements = new ArrayList<String>(watchedElementTypes);
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    private enum ParserPhase { PHASE_CAPTURE_ELEMENT, PHASE_CAPTURE_ATTRIBUTES }
    private ParserPhase parserPhase = ParserPhase.PHASE_CAPTURE_ELEMENT;

    private boolean isInString = false;
    private boolean nextIsEscaped = false;
    private int stringStartType = -1;
    List<String> watchedElements = list("a", "input", "form");

    private List<JSPElementReference> elementStack = list();
    JSPElementReference currentElement = null;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (type == '\'' || type == '"' && !nextIsEscaped) {
            if (stringStartType < 0) {
                stringStartType = type;
                isInString = true;
            } else if (type == stringStartType) {
                stringStartType = -1;
                isInString = false;
            }
        }
        nextIsEscaped = !nextIsEscaped && type == '\\';

        switch (parserPhase) {
            case PHASE_CAPTURE_ELEMENT:
                processCaptureElement(type, lineNumber, stringValue);
                break;

            case PHASE_CAPTURE_ATTRIBUTES:
                processCaptureAttributes(type, lineNumber, stringValue);
                break;
        }
    }

    private enum CaptureElementPhase { START, NAME, CLOSE }
    private CaptureElementPhase captureElementPhase = CaptureElementPhase.START;

    private void processCaptureElement(int type, int lineNumber, String stringValue) {
        switch (captureElementPhase) {
            case START:
                if (type == '<') {
                    captureElementPhase = CaptureElementPhase.NAME;
                }
                break;
            case NAME:
                if (type != '%' && stringValue != null && watchedElements.contains(stringValue)) {
                    currentElement = new JSPElementReference();
                    currentElement.setElementType(stringValue);
                    parserPhase = ParserPhase.PHASE_CAPTURE_ATTRIBUTES;
                } else if (type == '/') {

                } else {
                    currentElement = null;
                }
                captureElementPhase = CaptureElementPhase.START;
                break;
        }
    }

    private enum CaptureAttributesPhase {START, ATTR_VAL_START, ATTR_VAL, ATTR_VAL_END }
    private CaptureAttributesPhase captureAttributesPhase = CaptureAttributesPhase.START;

    String currentAttrName = null;
    String currentAttrVal = null;
    private void processCaptureAttributes(int type, int lineNumber, String stringValue) {
        switch (captureAttributesPhase) {
            case START:
                if (currentAttrName != null && currentAttrVal != null) {
                    currentElement.addAttribute(currentAttrName, currentAttrVal);
                }

                if (currentAttrName == null) {
                    if (stringValue != null) {
                        currentAttrName = CodeParseUtil.buildTokenString(type, stringValue);
                    } else if (!isValidInnerHtmlChar(type)) {
                        //  Must not be in an element, possibly CSS
                        parserPhase = ParserPhase.PHASE_CAPTURE_ELEMENT;
                        captureAttributesPhase = CaptureAttributesPhase.START;
                    }
                } else {
                    if (type == '=') {
                        captureAttributesPhase = CaptureAttributesPhase.ATTR_VAL_START;
                    } else {
                        currentAttrName += CodeParseUtil.buildTokenString(type, stringValue);
                    }
                }
                break;
            case ATTR_VAL_START:
                captureAttributesPhase = CaptureAttributesPhase.ATTR_VAL;
                break;
            case ATTR_VAL:
                if (!isInString) {
                    captureAttributesPhase = CaptureAttributesPhase.ATTR_VAL_END;
                } else {
                    currentAttrVal += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;
            case ATTR_VAL_END:
                currentElement.addAttribute(currentAttrName, currentAttrVal);
                currentAttrName = null;
                currentAttrVal = null;
                if (type == '>') {
                    parserPhase = ParserPhase.PHASE_CAPTURE_ELEMENT;
                }
                captureAttributesPhase = CaptureAttributesPhase.START;
                break;
        }
    }

    private boolean isValidInnerHtmlChar(int type) {
        return type == '=' || type == '\"' || type == '\'' || type == '-';
    }
}
