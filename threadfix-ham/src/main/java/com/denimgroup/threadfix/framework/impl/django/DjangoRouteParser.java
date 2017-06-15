package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StreamTokenizer;
import java.util.Map;
import java.util.StringTokenizer;

import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 5/12/2017.
 */
public class DjangoRouteParser implements EventBasedTokenizer{

    public static final SanitizedLogger LOG = new SanitizedLogger(DjangoRouteParser.class);
    public static final boolean logParsing = true;

    //alias, path
    private Map<String, String> importPathMap = map();
    //url, djangoroute
    private Map<String, DjangoRoute> routeMap = map();

    public static Map parse(@Nonnull File file) {
        DjangoRouteParser routeParser = new DjangoRouteParser();
        EventBasedTokenizerRunner.run(file, routeParser);
        return routeParser.routeMap;
    }

    private static final String
        IMPORT_START = "from",
        IMPORT = "import",
        ALIAS = "as",
        URL = "url",
        REGEXSTART = "r",
        INCLUDE = "include",
        TEMPLATE = "TemplateView.as_view",
        REDIRECT = "RedirectView.as_view",
        CACHE = "cache";

    private enum Phase {
        PARSING, IN_IMPORT, IN_URL
    }
    private Phase           currentPhase        = Phase.PARSING;
    private ImportState     currentImportState  = ImportState.START;
    private UrlState        currentUrlState     = UrlState.START;

    @Override
    public boolean shouldContinue() {
        return true;
    }

    private void log(Object string) {
        if (logParsing && string != null) {
            LOG.debug(string.toString());
        }
    }

    //each url file can reference other url files to be parsed, call recursively
    //urls are found in urlpatterns = [..] section with reference to view (controller)
    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        log("type  : " + type);
        log("string: " + stringValue);
        log("phase: " + currentPhase + " ");

        if (URL.equals(stringValue)){
            currentPhase = Phase.IN_URL;
            currentUrlState = UrlState.START;
        } else if (IMPORT_START.equals(stringValue)) {
            currentPhase = Phase.IN_IMPORT;
            currentImportState = ImportState.START;
        }

        switch (currentPhase) {
            case IN_IMPORT:
                processImport(type, stringValue);
                break;
            case IN_URL:
                processUrl(type, stringValue);
        }
    }

    private enum ImportState {
        START, ROOTIMPORTPATH, FILENAME, ALIASKEYWORD, ALIAS
    }
    private String alias, path;
    private void processImport(int type, String stringValue) {
        log(currentImportState);

        switch (currentImportState) {
            case START:
                alias = ""; path = "";
                if (IMPORT_START.equals(stringValue))
                    currentImportState = ImportState.ROOTIMPORTPATH;
                break;
            case ROOTIMPORTPATH:
                if (IMPORT.equals(stringValue))
                    currentImportState = ImportState.FILENAME;
                else
                    path = DjangoPathCleaner.cleanStringFromCode(stringValue);
                break;
            case FILENAME:
                if (ALIAS.equals(stringValue)) {
                    importPathMap.remove(alias);
                    alias = "";
                    currentImportState = ImportState.ALIAS;
                } else {
                    alias = stringValue;
                    path += "/" + stringValue + ".py";
                    importPathMap.put(alias, path);
                }
                break;
            case ALIAS:
                if (importPathMap.containsKey(alias)) importPathMap.remove(alias);

                if (type == StreamTokenizer.TT_WORD) {
                    alias += stringValue;
                    importPathMap.put(alias, path);
                } else if (type == '_') {
                    alias += "_";
                    importPathMap.put(alias, path);
                }
                break;
        }
    }

    private enum UrlState {
        START, REGEX, VIEWOPTIONS, VIEWPATH, INCLUDE, TEMPLATE, REDIRECT, CACHE
    }
    private StringBuilder regexBuilder;
    private boolean inRegex = false;
    private String viewPath = "";
    private void processUrl(int type, String stringValue) {
        log(currentUrlState);
        switch (currentUrlState) {
            case START:
                regexBuilder = new StringBuilder("");
                if (REGEXSTART.equals(stringValue))
                    currentUrlState = UrlState.REGEX;
                break;
            case REGEX:
                if (stringValue!= null && stringValue.startsWith("^")){
                    inRegex = true;
                    regexBuilder.append(stringValue.substring(1,stringValue.length()-1));
                    currentUrlState = UrlState.VIEWOPTIONS;
                } /*

                TODO adjust for when parameter values are in the regex

                else if (type == '(' || type == '$') {
                    inRegex = false;
                    currentUrlState = UrlState.VIEWOPTIONS;
                } else if (inRegex)
                    regexBuilder.append(stringValue);

                    */
                break;
            case VIEWOPTIONS:
                if (type != StreamTokenizer.TT_WORD) break;
                if (INCLUDE.equals(stringValue))
                    currentUrlState = UrlState.INCLUDE;
                else if (TEMPLATE.equals(stringValue))
                    currentUrlState = UrlState.TEMPLATE;
                else if (REDIRECT.equals(stringValue))
                    currentUrlState = UrlState.REDIRECT;
                else if (CACHE.equals(stringValue))
                    currentUrlState = UrlState.CACHE;
                else {
                    viewPath = stringValue;
                    currentUrlState = UrlState.VIEWPATH;
                }
                break;
            case VIEWPATH:
                if (type == StreamTokenizer.TT_WORD) {
                    viewPath += stringValue;
                } else if (type == '_') {
                    viewPath += "_";
                } else {
                    /*
                    should have two entries:
                    0 - controller(view) path
                    1 - method name
                     */
                    StringTokenizer tokenizer = new StringTokenizer(viewPath, ".");
                    String pathToken = tokenizer.nextToken();
                    String methodToken = tokenizer.nextToken();

                    if (importPathMap.containsKey(pathToken))
                        viewPath = importPathMap.get(pathToken);
                    else
                        viewPath = pathToken;

                    routeMap.put(regexBuilder.toString(), new DjangoRoute(regexBuilder.toString(), viewPath, methodToken, ""));
                    currentPhase = Phase.PARSING;
                    currentUrlState = UrlState.START;
                }
                break;
            case INCLUDE:
                //TODO lookup path in importpathmap, send file through this parser using regexBuilder.toString() as rootPath
                break;
            case TEMPLATE:
                //TODO
                break;
            case REDIRECT:
                //TODO
                break;
            case CACHE:
                //TODO
                break;
        }
    }
}
