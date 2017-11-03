package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsRoutesParser implements EventBasedTokenizer {

    static final SanitizedLogger LOG = new SanitizedLogger(RailsRoutesParser.class.getName());

    public static Collection<RailsRoute> run(File routesFile, Collection<RailsRouter> routers) {
        if (!routesFile.exists()) {
            LOG.error("File not found. Exiting. " + routesFile.getName());
            return null;
        }

        RailsRoutesParser parser = new RailsRoutesParser(routers);
        EventBasedTokenizerRunner.runRails(routesFile, true, false, parser);
        return parser.getRoutes();
    }

    public RailsRoutesParser(Collection<RailsRouter> routers) {
        this.routers = routers;
    }

    Collection<RailsRouter> routers;
    Collection<RailsRoute> routes = list();
    Collection<RailsScope> scopes = list();

    public Collection<RailsRoute> getRoutes() {
        return routes;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    String workingLine = "";
    int numOpenParen = 0;
    int numOpenBrace = 0;

    String lastString;
    int lastType;

    // Syntax is generally newline-sensitive except in the case of parameters, keep track of
    //  whether or not we're still parsing an entry even if a newline is reached.
    boolean spansNextLine = false;
    // Notes:
    //   Parameters continuing on the next line must end in a comma
    //          OK: get 'endpoint' , \n action: 'something'
    //      Not OK: get 'endpoint' \n , action: 'something'

    List<Object> scopeStack = list();

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '{') numOpenBrace++;
        if (type == '}') numOpenBrace--;
        if (type == '(') numOpenParen++;
        if (type == ')') numOpenParen--;

        if (currentEntry != null) {
            currentEntry.onToken(type, lineNumber, stringValue);
        }

        switch (parsePhase) {
            case SEARCH_IDENTIFIER:
                processSearchIdentifierPhase(type, lineNumber, stringValue);
                break;

            case PARAMETERS:
                processParametersPhase(type, lineNumber, stringValue);
                break;
        }


        if (stringValue != null) {
            lastString = stringValue;
        }

        if (type > 0) {
            lastType = type;
        }
    }

    RailsRoutingEntry makeEntryForIdentifier(String identifier) {
        RailsRoutingEntry result = null;
        for (RailsRouter router : routers) {
            result = router.identify(identifier);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    boolean isEntryScope() {
        return numOpenBrace == 0 && numOpenParen == 0;
    }

    enum ParsePhase { SEARCH_IDENTIFIER, PARAMETERS, INITIALIZER_PARAMETERS }
    ParsePhase parsePhase = ParsePhase.SEARCH_IDENTIFIER;


    RailsRoutingEntry currentEntry = null;


    void processSearchIdentifierPhase(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            currentEntry = makeEntryForIdentifier(stringValue);
            if (currentEntry != null) {
                parsePhase = ParsePhase.PARAMETERS;
                currentEntry.onBegin(stringValue);
            } else if (stringValue.equalsIgnoreCase("end")) {
                if (scopeStack.size() > 0) {
                    scopeStack.remove(scopeStack.size() - 1);
                }
            }
        }
    }

    RouteParameterValueType detectParameterType(String paramValue) {
        if (paramValue.startsWith(":")) {
            return RouteParameterValueType.SYMBOL;
        } else if (paramValue.startsWith("':")) {
            return RouteParameterValueType.SYMBOL_STRING_LITERAL;
        } else if (paramValue.startsWith("'")) {
            return RouteParameterValueType.STRING_LITERAL;
        } else {
            return RouteParameterValueType.UNKNOWN;
        }
    }

    String parameterLabel = null;
    void processParametersPhase(int type, int lineNumber, String stringValue) {

        if (parameterLabel == null && type == '(') {
            //  Initializer parameters ie 'scope(path_name: { add: 'other', remove: 'other2' })'
            parsePhase = ParsePhase.INITIALIZER_PARAMETERS;
            return;
        }

        spansNextLine = ((type == '\n' && lastType == ',') || !isEntryScope());

        if (type == '\n' && !spansNextLine) {
            //  TODO - Need to notify of last parameter
            parsePhase = ParsePhase.SEARCH_IDENTIFIER;
            currentEntry.onEnd();

//            if (AbstractRailsRoutingScope.class.isAssignableFrom(currentEntry.getClass())) {
//                AbstractRailsRoutingScope scope = (AbstractRailsRoutingScope)currentEntry;
//                scopeStack.add(scope);
//                scopes.add(buildScope(scope));
//            } else {
//                routes.addAll(buildRoutes((AbstractRailsRoutingEntry)currentEntry));
//            }

            currentEntry = null;
            parameterLabel = null;
            workingLine = "";
            return;
        }

        if (type == ',' && isEntryScope()) {
            if (workingLine.length() == 0) {
                workingLine = parameterLabel;
                parameterLabel = null;
            }

            RouteParameterValueType parameterType = detectParameterType(workingLine);
            currentEntry.onParameter(parameterLabel, workingLine, parameterType);
            parameterLabel = null;
            workingLine = "";
        } else {
            if (stringValue != null && parameterLabel == null) {
                parameterLabel = stringValue;
            }
            else {
                workingLine += CodeParseUtil.buildTokenString(type, stringValue);
            }
        }
    }

    void processInitializerParametersPhase(int type, int line, String stringValue) {
        if (isEntryScope()) { // Will occur exactly on the last closing paren ')'
            parsePhase = ParsePhase.PARAMETERS;
        }

        // TODO

    }

    Collection<RailsRoute> buildRoutes(AbstractRailsRoutingEntry entry) {
        return new ArrayList<RailsRoute>();
    }

//    RailsScope buildScope(AbstractRailsRoutingScope scope) {
//        return null;
//    }
}
