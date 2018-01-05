////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.routers.DjangoRouter;
import com.denimgroup.threadfix.framework.util.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.StreamTokenizer;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 5/12/2017.
 */
public class DjangoRouteParser implements EventBasedTokenizer{

    public static final SanitizedLogger LOG = new SanitizedLogger(DjangoRouteParser.class);
    public static final boolean logParsing = false;

    private PythonCodeCollection parsedCodebase;
    private DjangoRouterFactory routerFactory;
    private PythonModule thisModule;
    private PythonInterpreter interpreter;

    //alias, path
    private Map<String, String> importPathMap = map();
    //url, djangoroute
    private Map<String, DjangoRoute> routeMap = map();

    private Map<String, String> importAliases = null;

    private String sourceRoot;
    private String sourceFilePath;
    private String sourceFolderPath;

    private String rootPath = "";

    public DjangoRouteParser(String sourceRoot, String rootPath, String sourceFilePath, PythonCodeCollection sourcecode, PythonInterpreter loadedInterpreter) {
        this.sourceRoot = sourceRoot;
        this.rootPath = rootPath;
        this.parsedCodebase = sourcecode;
        this.sourceFilePath = sourceFilePath;
        this.sourceFolderPath = FilePathUtils.getFolder(new File(sourceFilePath));
        this.interpreter = loadedInterpreter;
        routerFactory = new DjangoRouterFactory(sourcecode);

        this.thisModule = sourcecode.findByFilePath(this.sourceFilePath);
        if (this.thisModule == null) {
            this.thisModule = new PythonModule();
            this.thisModule.setSourceCodePath(sourceFilePath);
        }
        importAliases = thisModule.getImports();
    }

    public static Map<String, DjangoRoute> parse(String sourceRoot, String rootPath, String sourceFilePath, PythonCodeCollection sourcecode, PythonInterpreter loadedInterpreter, @Nonnull File file) {
        DjangoRouteParser routeParser = new DjangoRouteParser(sourceRoot, rootPath, sourceFilePath, sourcecode, loadedInterpreter);
        EventBasedTokenizerRunner.run(file, routeParser);
        return routeParser.routeMap;
    }

    private static final String
        IMPORT_START = "from",
        IMPORT = "import",
        ALIAS = "as",
        URL = "url",
        URLPATTERNS = "urlpatterns",
        PATH = "path",
        RE_PATH = "re_path",
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

    private boolean isModuleScope() {
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

    private String expandSymbol(String symbolName) {
        Map<String, String> imports = thisModule.getImports();
        String varName = symbolName;
        String subNames = symbolName;
        if (varName.contains(".")) {
            varName = varName.substring(0, varName.indexOf("."));
            subNames = subNames.substring(subNames.indexOf(".") + 1);
        } else if (varName.contains("(")) {
            varName = varName.substring(0, varName.indexOf("("));
            subNames = null;
        } else {
            subNames = null;
        }

        String fullSymbol = imports.get(varName);
        if (subNames != null && fullSymbol != null) {
            fullSymbol += "." + subNames;
        }

        if (fullSymbol != null) {
            return fullSymbol;
        } else {
            return symbolName;
        }
    }

    private DjangoRouter findRouterForSymbol(String symbol) {
        if (symbol.contains(".")) {
            symbol = symbol.substring(0, symbol.indexOf("."));
        } else if (symbol.contains("(")) {
            symbol = symbol.substring(0, symbol.indexOf("("));
        }

        return namedRouters.get(symbol);
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

        if (URL.equals(stringValue) || PATH.equals(stringValue) || RE_PATH.equals(stringValue) || URLPATTERNS.equals(stringValue)){
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
        START, POSSIBLE_ROUTER, METHOD_CALL
    }
    ParsingState parsingState = ParsingState.START;
    private String possibleRouterName = null;
    int methodCall_numStartParen;
    String methodCallTarget = null;
    String methodCallName = null;
    List<String> methodCallParams;
    String workingMethodParam;

    private void processParsing(int type, String stringValue) {
        switch (parsingState) {
            case START:
                if (type == '=' && numOpenParen == 0) {
                    possibleRouterName = lastString;
                    parsingState = ParsingState.POSSIBLE_ROUTER;
                } else if (type == '(') {
                    parsingState = ParsingState.METHOD_CALL;
                    methodCall_numStartParen = numOpenParen;
                    methodCallParams = list();
                    if (lastString.contains(".")) {
                        methodCallTarget = lastString.substring(0, lastString.lastIndexOf("."));
                        methodCallName = lastString.substring(lastString.lastIndexOf(".") + 1);
                    } else {
                        methodCallTarget = null;
                        methodCallName = expandSymbol(lastString);
                    }
                    workingMethodParam = "";
                }
                break;

            case POSSIBLE_ROUTER:
                if (type == '\n' && numOpenParen == 0) {
                    parsingState = ParsingState.START;
                } else if (type == '(') {
                    String routerType = expandSymbol(lastString);
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

            case METHOD_CALL:
                if (numOpenParen < methodCall_numStartParen) {

                    if (workingMethodParam.length() > 0) {
                        methodCallParams.add(workingMethodParam);
                    }

                    //  Fully-qualify these param values
                    for (int i = 0; i < methodCallParams.size(); i++) {
                        String param = methodCallParams.get(i);
                        param = expandSymbol(param);
                        methodCallParams.set(i, param);
                    }

                    DjangoRouter routerForVar = namedRouters.get(methodCallTarget);
                    if (routerForVar != null) {
                        routerForVar.parseMethod(methodCallName, methodCallParams);
                    }
                    methodCall_numStartParen = -1;
                    methodCallName = null;
                    methodCallTarget = null;
                    methodCallParams.clear();
                    workingMethodParam = null;
                    parsingState = ParsingState.START;
                } else {
                    if (type == ',' && numOpenParen == methodCall_numStartParen) {
                        methodCallParams.add(workingMethodParam.trim());
                        workingMethodParam = "";
                    } else {
                        workingMethodParam += CodeParseUtil.buildTokenString(type, stringValue);
                    }
                }
                break;
        }
    }

    private void importRouters(File fromFile) {

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
                    path = DjangoPathUtil.combine(path, stringValue);
                    if (!(new File(DjangoPathUtil.combine(sourceFolderPath, path) + ".py")).exists()) {
                        if ((new File(DjangoPathUtil.combine(sourceFolderPath, basePath) + ".py")).exists()) {
                            path = basePath;
                        }
                    }

                    String filePath = DjangoPathUtil.combine(sourceFolderPath, path + ".py");
                    File codeFile = new File(filePath);
                    if (codeFile.exists()) {
                        DjangoRouteParser parser = new DjangoRouteParser(sourceRoot, rootPath, codeFile.getAbsolutePath(), parsedCodebase, interpreter);
                        EventBasedTokenizerRunner.run(codeFile, PythonTokenizerConfigurator.INSTANCE, parser);
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
                if (type == '(')
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

                        File controller = null;
                        AbstractPythonStatement pythonController = parsedCodebase.findByFullName(expandSymbol(pathToken));
                        if (pythonController != null) {
                            if (pythonController.getSourceCodePath() != null) {
                                controller = new File(pythonController.getSourceCodePath());
                            }
                        } else {
                            controller = new File(sourceRoot, viewPath + ".py");
                        }

                        if (controller != null && controller.exists()) {
                            String urlPath = DjangoPathUtil.combine(rootPath, regexBuilder.toString());
                            routeMap.put(urlPath, DjangoControllerParser.parse(controller, urlPath, methodToken));
                        }
                    } else if (parsedCodebase != null) {
                        File controller = null;
                        AbstractPythonStatement pythonController = parsedCodebase.findByFullName(expandSymbol(viewPath));
                        if (pythonController != null) {
                            controller = new File(pythonController.getSourceCodePath());
                        }

                        if (controller != null && controller.exists()) {
                            String urlPath = DjangoPathUtil.combine(rootPath, regexBuilder.toString());
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

                    DjangoRouter referencedRouter = findRouterForSymbol(viewPath);
                    if (referencedRouter != null && viewPath.endsWith(referencedRouter.getUrlsName())) {
                        String basePath = DjangoPathUtil.combine(rootPath, regexBuilder.toString());
                        for (DjangoRoute route : referencedRouter.getRoutes()) {
                            String fullPath = DjangoPathUtil.combine(basePath, route.getUrl());
                            DjangoRoute newRoute = new DjangoRoute(fullPath, route.getViewPath());
                            newRoute.setLineNumbers(route.getStartLineNumber(), route.getEndLineNumber());
                            for (Map.Entry<String, ParameterDataType> param : newRoute.getParameters().entrySet()) {
                                newRoute.addParameter(param.getKey(), param.getValue());
                            }
                            routeMap.put(fullPath, newRoute);
                        }
                    } else {

                        String basePath = DjangoPathUtil.combine(rootPath, regexBuilder.toString());
                        AbstractPythonStatement referencedStatement = parsedCodebase.resolveLocalSymbol(viewPath, thisModule);
                        //interpreter.getExecutionContext().res
                        if (referencedStatement != null && referencedStatement instanceof PythonPublicVariable) {

                            String fullReferencedPath = parsedCodebase.expandSymbol(viewPath, thisModule);
                            PythonValue interpretedValues = interpreter.run(fullReferencedPath);

                            if (interpretedValues instanceof PythonVariable) {
                                interpretedValues = interpreter.getExecutionContext().resolveAbsoluteValue(interpretedValues);
                            }

                            if (interpretedValues instanceof PythonArray) {
                                PythonArray array = (PythonArray)interpretedValues;
                                for (PythonObject entry : array.getValues(PythonObject.class)) {
                                    PythonValue pattern = entry.getMemberValue("pattern");
                                    PythonValue view = entry.getMemberValue("view");
                                    AbstractPythonStatement viewSource = InterpreterUtil.tryGetSource(view);

                                    if (pattern == null || view == null ||
                                            !(pattern instanceof PythonStringPrimitive) ||
                                            !(view instanceof PythonVariable)) {
                                        continue;
                                    }

                                    String patternText = ((PythonStringPrimitive) pattern).getValue();
                                    String newEndpoint = DjangoPathUtil.combine(basePath, patternText);
                                    String viewPath = viewSource == null ? null : viewSource.getSourceCodePath();
                                    if (viewPath == null) {
                                        // Can't use this route for HAM but still useful for endpoint detection
                                        viewPath = "";
                                    }
                                    DjangoRoute newRoute = new DjangoRoute(newEndpoint, viewPath);
                                    if (viewSource != null) {
                                        newRoute.setLineNumbers(viewSource.getSourceCodeStartLine(), viewSource.getSourceCodeEndLine());
                                    }
                                    routeMap.put(newEndpoint, newRoute);
                                }
                            }

//                            PythonPublicVariable referencedVar = (PythonPublicVariable) referencedStatement;
//                            String value = referencedVar.getValueString();
//                            if (value.startsWith("[") && value.endsWith("]")) {
//                                value = value.substring(1, value.length() - 1);
//                            }
//
//                            String[] parts = CodeParseUtil.splitByComma(value);
//                            for (String part : parts) {
//                                if (part.startsWith("url(")) {
//                                    part = part.substring("url(".length(), part.length() - 1);
//                                    String[] params = CodeParseUtil.splitByComma(part);
//                                    String endpoint = params[0].trim();
//                                    String controller = params[1].trim();
//
//                                    AbstractPythonStatement resolvedController = parsedCodebase.findByPartialName(thisModule, controller);
//                                    if (resolvedController == null) {
//                                        resolvedController = parsedCodebase.findByFullName(controller);
//                                    }
//                                    if (resolvedController == null) {
//                                        continue;
//                                    }
//
//                                    if (endpoint.startsWith("r")) {
//                                        endpoint = endpoint.substring(2);
//                                    } else if (endpoint.startsWith("'") || endpoint.startsWith("\"")) {
//                                        endpoint = endpoint.substring(1);
//                                    }
//
//                                    if (endpoint.endsWith("'") || endpoint.endsWith("\"")) {
//                                        endpoint = endpoint.substring(0, endpoint.length() - 1);
//                                    }
//
//                                    endpoint = DjangoPathUtil.combine(basePath, endpoint);
//                                    DjangoRoute newRoute = new DjangoRoute(endpoint, resolvedController.getSourceCodePath());
//                                    newRoute.setLineNumbers(resolvedController.getSourceCodeStartLine(), resolvedController.getSourceCodeEndLine());
//                                    routeMap.put(endpoint, newRoute);
//                                }
//                            }
                        } else if (referencedStatement instanceof PythonFunctionCall) {

                            if (referencedStatement.getSourceCodePath() != null && referencedStatement.getSourceCodeStartLine() > -1) {

                                PythonFunction referencedFunction = (PythonFunction)referencedStatement;
                                PythonValue functionResult = interpreter.run(viewPath, referencedFunction, null);
                                functionResult = interpreter.getExecutionContext().resolveAbsoluteValue(functionResult);

                                if (functionResult instanceof PythonArray) {
                                    log("Found PythonArray");
                                } else if (functionResult instanceof PythonObject) {
                                    log("Found PythonObject");
                                }
                            }

                        } else {

                            String viewFile = null;
                            if (importPathMap.containsKey(viewPath)) {
                                viewFile = importPathMap.get(viewPath);
                            } else {
                                viewFile = viewPath.replaceAll("\\.", "\\/");
                                if (!new File(DjangoPathUtil.combine(sourceRoot, viewFile)).exists()) {
                                    viewFile += ".py";
                                }
                            }

                            File importFile = new File(DjangoPathUtil.combine(sourceRoot, viewFile));
                            if (importFile.exists()) {
                                if (importFile.isDirectory()) {
                                    for (File file : importFile.listFiles())
                                        routeMap.putAll(DjangoRouteParser.parse(sourceRoot, DjangoPathUtil.combine(rootPath, regexBuilder.toString()), file.getAbsolutePath(), parsedCodebase, interpreter, file));
                                } else {
                                    routeMap.putAll(DjangoRouteParser.parse(sourceRoot, DjangoPathUtil.combine(rootPath, regexBuilder.toString()), importFile.getAbsolutePath(), parsedCodebase, interpreter, importFile));
                                }
                            }
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
