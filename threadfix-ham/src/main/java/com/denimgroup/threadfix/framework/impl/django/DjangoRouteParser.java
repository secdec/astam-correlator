// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonScope;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonModule;
import com.denimgroup.threadfix.framework.util.*;
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
    public static final boolean logParsing = false;

    private PythonCodeCollection parsedCodebase;
    private DjangoRouterFactory routerFactory;
    PythonModule rootModule;

    //alias, path
    private Map<String, String> importPathMap = map();
    //url, djangoroute
    private Map<String, DjangoRoute> routeMap = map();

    private String sourceRoot;
    private String sourceFolderPath;

    private String rootPath = "";

    public DjangoRouteParser(String sourceRoot, String rootPath, String sourceFolderPath, PythonCodeCollection sourcecode) {
        this.sourceRoot = sourceRoot;
        this.rootPath = rootPath;
        this.parsedCodebase = sourcecode;
        this.sourceFolderPath = sourceFolderPath;
        routerFactory = new DjangoRouterFactory(sourcecode);
        this.rootModule = sourcecode.findFirstByFilePath(sourceFolderPath, PythonModule.class);
    }

    public static Map<String, DjangoRoute> parse(String sourceRoot, String rootPath, String sourceFolderPath, PythonCodeCollection sourcecode, @Nonnull File file) {
        DjangoRouteParser routeParser = new DjangoRouteParser(sourceRoot, rootPath, sourceFolderPath, sourcecode);
        EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, routeParser);
        return routeParser.routeMap;
    }

    private static final String
        IMPORT_START = "from",
        IMPORT = "import",
        ALIAS = "as",
        URL = "url",
        URLPATTERNS = "urlpatterns",
        REGEXSTART = "r",
        INCLUDE = "include",
        TEMPLATE = "TemplateView.as_view",
        REDIRECT = "RedirectView.as_view",
        CACHE = "cache";

    private enum Phase {
        PARSING, IN_IMPORT, IN_URL, IN_COMMENT
    }
    private Phase           currentPhase        = Phase.PARSING;
    private ImportState     currentImportState  = ImportState.START;
    private UrlState        currentUrlState     = UrlState.START;

    private int     numOpenParen    = 0;
    private int     numOpenBracket  = 0;
    private int     numOpenBrace    = 0;
    private boolean isInString      = false;

    private String lastString = null;
    private int lastType = -1;

    private Map<String, DjangoRouter> namedRouters = map();

    private boolean getRootModule() {
        return numOpenParen == 0 && numOpenBracket == 0 && numOpenBrace == 0;
    }

    private boolean isInMainParams() {
        return numOpenParen == 1 && numOpenBracket == 0 && numOpenBrace == 0 && !isInString;
    }

    private boolean isInParamExpression() {
        return numOpenParen >= 1 && (numOpenBracket > 0 || numOpenBrace > 0 || isInString);
    }

    private boolean isInArray() {
        return numOpenBracket > 0;
    }

    private boolean isInObjectLiteral() {
        return numOpenBrace > 0;
    }

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

        if (type == '(') ++numOpenParen;
        if (type == ')') --numOpenParen;
        if (type == '{') ++numOpenBrace;
        if (type == '}') --numOpenBrace;
        if (type == '[') ++numOpenBracket;
        if (type == ']') --numOpenBracket;

        boolean isQuote = type == '\'' || type == '"';
        if (isQuote) {
            isInString = !isInString;
            if (stringValue != null) {
                //  If 'type' is provided and 'stringValue' != null, then 'stringValue' is surrounded by character 'type'
                isInString = !isInString;
            }
        }

        if (URL.equals(stringValue) || URLPATTERNS.equals(stringValue)){
            currentPhase = Phase.IN_URL;
            currentUrlState = UrlState.START;
        } else if (IMPORT_START.equals(stringValue)) {
            currentPhase = Phase.IN_IMPORT;
            currentImportState = ImportState.START;
        } else if (type == '#') {
            currentPhase = Phase.IN_COMMENT;
        }

        switch (currentPhase) {
            case PARSING:
                processParsing(type, stringValue);
                break;
            case IN_IMPORT:
                processImport(type, stringValue);
                break;
            case IN_URL:
                processUrl(type, stringValue);
                break;
            case IN_COMMENT:
                break;
        }

        if (stringValue != null) lastString = stringValue;
        if (type > 0) lastType = type;
    }

    private enum ParsingState {
        START, POSSIBLE_ROUTER
    }
    ParsingState parsingState = ParsingState.START;
    private String possibleRouterName = null;

    private void processParsing(int type, String stringValue) {
        switch (parsingState) {
            case START:
                if (type == '=' && numOpenParen == 0) {
                    possibleRouterName = lastString;
                    parsingState = ParsingState.POSSIBLE_ROUTER;
                }
                break;

            case POSSIBLE_ROUTER:
                if (type == '\n' && numOpenParen == 0) {
                    parsingState = ParsingState.START;
                } else if (type == '(') {
                    String routerType = lastString;
                    //  Remove any module names from the type name
                    if (routerType.contains(".")) {
                        routerType = routerType.substring(routerType.lastIndexOf(".") + 1);
                    }
                    DjangoRouter router = routerFactory.makeRouterFor(routerType);
                    if (router != null) {
                        namedRouters.put(possibleRouterName, router);
                    }
                    possibleRouterName = null;
                    parsingState = ParsingState.START;
                }
                break;
        }
    }

    private enum ImportState {
        START, ROOTIMPORTPATH, FILENAME, ALIASKEYWORD, ALIAS
    }
    private String alias, path;
    private void processImport(int type, String stringValue) {
        log(currentImportState);

        if (type == '\n') {
            currentImportState = ImportState.START;
            currentPhase = Phase.PARSING;
            return;
        }

        switch (currentImportState) {
            case START:
                alias = ""; path = "";
                if (IMPORT_START.equals(stringValue))
                    currentImportState = ImportState.ROOTIMPORTPATH;
                break;
            case ROOTIMPORTPATH:
                if (IMPORT.equals(stringValue))
                    currentImportState = ImportState.FILENAME;
                else if (stringValue != null)
                    path = DjangoPathCleaner.cleanStringFromCode(stringValue);
                break;
            case FILENAME:
                if (ALIAS.equals(stringValue)) {
                    importPathMap.remove(alias);
                    alias = "";
                    currentImportState = ImportState.ALIAS;
                } else if (stringValue != null){
                    alias = stringValue;
                    String basePath = path;
                    path = PathUtil.combine(path, stringValue);
                    if (!(new File(PathUtil.combine(sourceFolderPath, path) + ".py")).exists()) {
                        if ((new File(PathUtil.combine(sourceFolderPath, basePath) + ".py")).exists()) {
                            path = basePath;
                        }
                    }

                    String filePath = PathUtil.combine(sourceFolderPath, path + ".py");
                    File codeFile = new File(filePath);
                    if (codeFile.exists()) {
                        DjangoRouteParser parser = new DjangoRouteParser(sourceRoot, rootPath, FilePathUtils.getFolder(codeFile), parsedCodebase);
                        EventBasedTokenizerRunner.run(codeFile, DjangoTokenizerConfigurator.INSTANCE, parser);
                        if (parser.namedRouters != null) {
                            namedRouters.putAll(parser.namedRouters);
                        }
                    }
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
                    String regexValue = stringValue;
                    //  Route matcher can now use full regex strings
//                    if (regexValue.startsWith("^"))
//                        regexValue = regexValue.substring(1);
//                    if (regexValue.endsWith("$"))
//                        regexValue = regexValue.substring(0, regexValue.length() - 1);
                    regexBuilder.append(regexValue);
                    currentUrlState = UrlState.VIEWOPTIONS;
                } /*

                TODO adjust to handle regex values

                else if (type == '(' || type == '$') {
                    inRegex = false;
                    currentUrlState = UrlState.VIEWOPTIONS;
                } else if (inRegex)
                    regexBuilder.append(stringValue);

                    */
                break;
            case VIEWOPTIONS:
                if (type != StreamTokenizer.TT_WORD) break;
                if (INCLUDE.equals(stringValue)) {
                    viewPath = "";
                    currentUrlState = UrlState.INCLUDE;
                } else if (TEMPLATE.equals(stringValue))
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
                    //run through controller parser
                    /*
                    should have two entries:
                    0 - controller(view) path
                    1 - method name
                     */
                    if (viewPath.contains(".")) {
                        StringTokenizer tokenizer = new StringTokenizer(viewPath, ".");
                        String pathToken = tokenizer.nextToken();
                        String methodToken = tokenizer.nextToken();

                        if (importPathMap.containsKey(pathToken))
                            viewPath = importPathMap.get(pathToken);
                        else
                            viewPath = pathToken;

                        File controller;
                        AbstractPythonScope pythonController = parsedCodebase.findByFullName(pathToken);
                        if (pythonController != null) {
                            controller = new File(pythonController.getSourceCodePath());
                        } else {
                            controller = new File(sourceRoot, viewPath + ".py");
                        }

                        if (controller.exists()) {
                            String urlPath = PathUtil.combine(rootPath, regexBuilder.toString());
                            routeMap.put(urlPath, DjangoControllerParser.parse(controller, urlPath, methodToken));
                        }
                    } else if (parsedCodebase != null) {
                        File controller = null;
                        AbstractPythonScope pythonController = parsedCodebase.findByFullName(viewPath);
                        if (pythonController != null) {
                            controller = new File(pythonController.getSourceCodePath());
                        }

                        if (controller != null && controller.exists()) {
                            String urlPath = PathUtil.combine(rootPath, regexBuilder.toString());
                            routeMap.put(urlPath, DjangoControllerParser.parse(controller, urlPath, null));
                        }
                    }

                    currentPhase = Phase.PARSING;
                    currentUrlState = UrlState.START;
                }
                break;
            case INCLUDE:
                //run back through url parser
                if (type == StreamTokenizer.TT_WORD || ((type == '\'' || type =='"') && stringValue != null)) {
                    viewPath += stringValue;
                } else if (type == '_') {
                    viewPath += "_";
                } else if (!viewPath.isEmpty()) {
                    String viewFile = null;
                    if (importPathMap.containsKey(viewPath)) {
                        viewFile = importPathMap.get(viewPath);
                    } else {
                        viewFile = viewPath.replaceAll("\\.", "\\/");
                        if (!new File(PathUtil.combine(sourceRoot, viewFile)).exists()) {
                            viewFile += ".py";
                        }
                    }

                    File importFile = new File(PathUtil.combine(sourceRoot, viewFile));
                    if (importFile.exists()) {
                        if (importFile.isDirectory()) {
                            for (File file : importFile.listFiles())
                                routeMap.putAll(DjangoRouteParser.parse(sourceRoot, PathUtil.combine(rootPath, regexBuilder.toString()), FilePathUtils.getFolder(file), parsedCodebase, file));
                        } else {
                            routeMap.putAll(DjangoRouteParser.parse(sourceRoot, PathUtil.combine(rootPath, regexBuilder.toString()), FilePathUtils.getFolder(importFile), parsedCodebase, importFile));
                        }
                    }

                    regexBuilder = new StringBuilder();
                    viewPath = "";
                    currentUrlState = UrlState.START;
                }
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
