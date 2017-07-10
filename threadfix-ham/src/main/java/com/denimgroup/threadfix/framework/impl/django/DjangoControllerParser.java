package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StreamTokenizer;

/**
 * Created by csotomayor on 6/21/2017.
 */
public class DjangoControllerParser implements EventBasedTokenizer {

    public static final SanitizedLogger LOG = new SanitizedLogger(DjangoControllerParser.class);
    public static final boolean logParsing = false;

    private DjangoRoute djangoRoute;
    private String methodName = "";
    private boolean shouldContinue = true;

    public static DjangoRoute parse(@Nonnull File file, String url, String methodName) {
        DjangoControllerParser controllerParser = new DjangoControllerParser();
        controllerParser.djangoRoute = new DjangoRoute(url, file.getAbsolutePath());
        controllerParser.methodName = methodName;
        EventBasedTokenizerRunner.run(file, controllerParser);
        return controllerParser.djangoRoute;
    }

    private static final String
            METHOD_DEF = "def",
            REQUEST = "request",
            GETREQUEST = "GET",
            POSTREQUEST = "POST";

    private enum Phase {
        PARSING, IN_METHOD
    }
    private Phase           currentPhase        = Phase.PARSING;
    private MethodState     currentMethodState  = MethodState.START;

    @Override
    public boolean shouldContinue() {
        return shouldContinue;
    }

    private void log(Object string) {
        if (logParsing && string != null) {
            LOG.debug(string.toString());
        }
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        log("type  : " + type);
        log("string: " + stringValue);
        log("phase: " + currentPhase + " ");

        if (METHOD_DEF.equals(stringValue)){
            currentPhase = Phase.IN_METHOD;
            currentMethodState = MethodState.START;
        }

        switch (currentPhase) {
            case IN_METHOD:
                processMethod(type, stringValue);
                break;
        }
    }

    private enum MethodState {
        START, PARAMS, BODY, REQUEST, PARAM
    }
    private void processMethod(int type, String stringValue) {
        log(currentMethodState);

        switch (currentMethodState) {
            case START:
                if (METHOD_DEF.equals(stringValue))
                    break;

                if (methodName.equals(stringValue))
                    currentMethodState = MethodState.PARAMS;
                else
                    currentPhase = Phase.PARSING;

                break;
            case PARAMS:
                if (type == ')')
                    currentMethodState = MethodState.BODY;
                else if (type == StreamTokenizer.TT_WORD){
                    if (REQUEST.equals(stringValue))
                        break;
                    djangoRoute.addParameter(stringValue);
                }
                break;
            case BODY:
                if (type == StreamTokenizer.TT_WORD && stringValue.contains(REQUEST)) {
                    if (stringValue.contains(GETREQUEST)) {
                        djangoRoute.addHttpMethod(GETREQUEST);
                        currentMethodState = MethodState.PARAM;
                    } else if (stringValue.contains(POSTREQUEST)) {
                        djangoRoute.addHttpMethod(POSTREQUEST);
                        currentMethodState = MethodState.PARAM;
                    }
                }
                break;
            case PARAM:
                if (type == ')') {
                    currentPhase = Phase.IN_METHOD;
                    currentMethodState = MethodState.BODY;
                } else if (stringValue != null && !stringValue.isEmpty()) {
                    djangoRoute.addParameter(stringValue);
                    currentPhase = Phase.IN_METHOD;
                    currentMethodState = MethodState.BODY;
                }
        }
    }

}
