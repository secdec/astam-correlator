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

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.DjangoApiConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCachingExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.ExpressionDeconstructor;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.framework.util.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 4/27/2017.
 */
public class DjangoEndpointGenerator implements EndpointGenerator{

    private static final SanitizedLogger LOG = new SanitizedLogger(DjangoEndpointGenerator.class);

    private List<Endpoint> endpoints;
    private Map<String, DjangoRoute> routeMap;

    private File rootDirectory, rootUrlsFile;
    private List<File> possibleGuessedUrlFiles;

    private void debugLog(String msg) {
        LOG.info(msg);
        //LOG.debug(msg);
    }

    public DjangoEndpointGenerator(@Nonnull File rootDirectory) {
        assert rootDirectory.exists() : "Root file did not exist.";
        assert rootDirectory.isDirectory() : "Root file was not a directory.";



        long generationStartTime = System.currentTimeMillis();

        this.rootDirectory = rootDirectory;

        findRootUrlsFile();
        if (rootUrlsFile == null || !rootUrlsFile.exists()) {
            possibleGuessedUrlFiles = findUrlsByFileName();
        }

        boolean foundUrlFiles = (rootUrlsFile != null && rootUrlsFile.exists()) || (possibleGuessedUrlFiles != null && possibleGuessedUrlFiles.size() > 0);
        assert foundUrlFiles : "Root URL file did not exist";

        LOG.info("Parsing codebase for modules, classes, and functions...");
        long codeParseStartTime = System.currentTimeMillis();
        PythonCodeCollection codebase = PythonSyntaxParser.run(rootDirectory);
        //PythonDebugUtil.printFullTypeNames(codebase);
        //PythonDebugUtil.printFullImports(codebase);
        long codeParseDuration = System.currentTimeMillis() - codeParseStartTime;
        LOG.info("Finished parsing codebase in " + codeParseDuration + "ms, found "
                + codebase.getModules().size() + " modules, "
                + codebase.getClasses().size() + " classes, "
                + codebase.getFunctions().size() + " functions, "
                + codebase.getPublicVariables().size() + " public variables, "
                + codebase.get(PythonVariableModification.class).size() + " variable changes, and "
                + codebase.get(PythonFunctionCall.class).size() + " function calls.");

        debugLog("Attaching known Django APIs");
        DjangoApiConfigurator.apply(codebase);

        codebase.initialize();

        DjangoApiConfigurator.applyPostLink(codebase);

        long executionStartTime = System.currentTimeMillis();

        ExpressionDeconstructor ed = new ExpressionDeconstructor();
        List<String> subexpressions;
        subexpressions = ed.deconstruct("abc = 123 + 23");
        subexpressions = ed.deconstruct("[a, c, d]");
        subexpressions = ed.deconstruct("a, c, d", 3);
        subexpressions = ed.deconstruct("a == b");
        subexpressions = ed.deconstruct("var += 2");
        subexpressions = ed.deconstruct("var -= '2' - 5", 2);
        subexpressions = ed.deconstruct("abc = [1, 2, 3] + [2]");
        subexpressions = ed.deconstruct("12, '13', someCall(5 + 3, this.x), this.y, a.b.c.d");
        subexpressions = ed.deconstruct("[1, 2, 3], 5", 1);
        subexpressions = ed.deconstruct("someCall(123).member");
        subexpressions = ed.deconstruct("someCall(123).otherCall().member");
        subexpressions = ed.deconstruct("123 + 34.12 - x");

        PythonExpressionParser incrementalParser = new PythonExpressionParser();
        //PythonCachingExpressionParser incrementalParser = new PythonCachingExpressionParser();
        PythonExpression expr;
        expr = incrementalParser.processString("x = 5");
        expr = incrementalParser.processString("x += 5");
        expr = incrementalParser.processString("x.y += 5");
        expr = incrementalParser.processString("x.y += a - b");
        expr = incrementalParser.processString("x, y = (5, 6)");
        expr = incrementalParser.processString("x, y = (5, 6,)");
        expr = incrementalParser.processString("'abc %s' % ('123',)");
        expr = incrementalParser.processString("abc(xyz)");
        expr = incrementalParser.processString("a.b.c(xyz)");
        expr = incrementalParser.processString("a.b.c(x, y)");
        expr = incrementalParser.processString("a.b.c(x.y.z)");
        expr = incrementalParser.processString("a.b.c(x, y.z) + 5");
        expr = incrementalParser.processString("a.b.c(d(x), g(y).z) -= 2");
        expr = incrementalParser.processString("a.b(d(x).y + 5, g(y) -= 5).x + 1 % 5 - g(x) + (z, b)");
        expr = incrementalParser.processString("x - (y - (z - w)) + v");
        expr = incrementalParser.processString("a[123]");
        expr = incrementalParser.processString("[1], [2]");

        expr = incrementalParser.processString("if page and page.parent_id:");
        expr = incrementalParser.processString("title=_(u\"New page\")");
        expr = incrementalParser.processString("context['has_change_permissions'] = user_can_change_page(request.user, page)");
        expr = incrementalParser.processString("abc['123' + '456' - func() + func].func() + 2");

        expr = incrementalParser.processString("abc %= ('1', '2', '3')");

        expr = incrementalParser.processString("return x = 5", null);
        expr = incrementalParser.processString("return", null);
        expr = incrementalParser.processString("return x, y", null);

        expr = incrementalParser.processString("cms.utils.get_cms_setting(\"CACHE_PREFIX\") + 'CMS_PAGE_CACHE_VERSION'");
        expr = incrementalParser.processString("cms.utils.get_cms_setting(\"CACHE_PREFIX\") + 'CMS_PAGE_CACHE_VERSION'");
        expr = incrementalParser.processString("cms.utils.get_cms_setting(\"CACHE_PREFIX\") + 'CMS_PAGE_CACHE_VERSION'");
        expr = incrementalParser.processString("cms.utils.get_cms_setting(\"CACHE_PREFIX\") + 'CMS_PAGE_CACHE_VERSION'");

        expr = incrementalParser.processString("re.compile(r'[^a-zA-Z0-9_-]')");
        expr = incrementalParser.processString("re.compile(r'[^a-zA-Z0-9_-]')");
        expr = incrementalParser.processString("re.compile(r'[^a-zA-Z0-9_-]')");

        long executionDuration = System.currentTimeMillis() - executionStartTime;
        LOG.info("Executing test expressions took " + executionDuration + "ms");

        executionStartTime = System.currentTimeMillis();

        PythonInterpreter interpreter = new PythonInterpreter(codebase);
        int testCount = 200;
        //for (int i = 0; i < testCount; i++) {
            PythonValue intrpval;
            intrpval = interpreter.run("a1, a2 = [1], [2]");
            intrpval = interpreter.run("x = 5");
            intrpval = interpreter.run("x, y = (1, 2)");
            intrpval = interpreter.run("xx, yy = [3, 4]");
            intrpval = interpreter.run("a, b = (x, y)");
            intrpval = interpreter.run("b1, b2 = (x + xx, y + yy + 3)");
            intrpval = interpreter.run("b1 += 5");
            intrpval = interpreter.run("b1 -= 2");
            intrpval = interpreter.run("c1 = [5]");
            intrpval = interpreter.run("c1, c2 = (c1 + a1, '3')");
            intrpval = interpreter.run("c1[1] = 'a'");
            intrpval = interpreter.run("m = { 'a': 1 } ");
            intrpval = interpreter.run("'abc %s' % (123)");
            intrpval = interpreter.run("'abc %s, %s' % (123, '456')");
            intrpval = interpreter.run("5 + 5");
            intrpval = interpreter.run("'123' + 'abc'");
            intrpval = interpreter.run("123 + 5 # - 5");
            intrpval = interpreter.run("");
            intrpval = interpreter.run("# asd");
            intrpval = interpreter.run("5+2#asd");
            intrpval = interpreter.run("4");
            intrpval = interpreter.run("3#asd");
            intrpval = interpreter.run("[1] + [2]");
            intrpval = interpreter.run("a1 + a2");
        //}

        executionDuration = System.currentTimeMillis() - executionStartTime;
        LOG.info("Running text interpreter expressions " + testCount + " times took " + executionDuration + "ms");

        interpreter = new PythonInterpreter(codebase);
        runInterpreterOnNonDeclarations(codebase, interpreter);

        DjangoInternationalizationDetector i18Detector = new DjangoInternationalizationDetector();
        codebase.traverse(i18Detector);
        if (i18Detector.isLocalized()) {
            LOG.info("Internationalization detected");
        }

        if (rootUrlsFile != null && rootUrlsFile.exists()) {
            routeMap = DjangoRouteParser.parse(rootDirectory.getAbsolutePath(), "", rootUrlsFile.getAbsolutePath(), codebase, interpreter, rootUrlsFile);
        } else if (possibleGuessedUrlFiles != null && possibleGuessedUrlFiles.size() > 0) {

            debugLog("Found " + possibleGuessedUrlFiles.size() + " possible URL files:");
            for (File urlFile : possibleGuessedUrlFiles) {
                debugLog("- " + urlFile.getAbsolutePath());
            }

            routeMap = map();
            for (File guessedUrlsFile : possibleGuessedUrlFiles) {
                Map<String, DjangoRoute> guessedUrls = DjangoRouteParser.parse(rootDirectory.getAbsolutePath(), "", guessedUrlsFile.getAbsolutePath(), codebase, interpreter, guessedUrlsFile);
                for (Map.Entry<String, DjangoRoute> url : guessedUrls.entrySet()) {
                    DjangoRoute existingRoute = routeMap.get(url.getKey());
                    if (existingRoute != null) {
                        Collection<String> existingHttpMethods = existingRoute.getHttpMethods();
                        Map<String, ParameterDataType> existingParams = existingRoute.getParameters();
                        for (String httpMethod : url.getValue().getHttpMethods()) {
                            if (!existingHttpMethods.contains(httpMethod)) {
                                existingHttpMethods.add(httpMethod);
                            }
                        }
                        for (Map.Entry<String, ParameterDataType> param : url.getValue().getParameters().entrySet()) {
                            if (!existingParams.containsKey(param.getKey())) {
                                existingParams.put(param.getKey(), param.getValue());
                            }
                        }
                    } else {
                        routeMap.put(url.getKey(), url.getValue());
                    }
                }
            }
        } else {
            routeMap = map();
        }

        this.endpoints = generateMappings(i18Detector.isLocalized());

        long generationDuration = System.currentTimeMillis() - generationStartTime;
        debugLog("Finished python endpoint generation in " + generationDuration + "ms");
    }

    private void findRootUrlsFile() {
        File manageFile = new File(rootDirectory, "manage.py");
        assert manageFile.exists() : "manage.py does not exist in root directory";
        SettingsFinder settingsFinder = new SettingsFinder();
        EventBasedTokenizerRunner.run(manageFile, settingsFinder);

        File settingsFile = settingsFinder.getSettings(rootDirectory.getPath());
        //assert settingsFile.exists() : "Settings file not found";
        UrlFileFinder urlFileFinder = new UrlFileFinder();

        if (settingsFile.isDirectory()) {
            for (File file : settingsFile.listFiles()) {
                EventBasedTokenizerRunner.run(file, PythonTokenizerConfigurator.INSTANCE, urlFileFinder);
                if (!urlFileFinder.shouldContinue()) break;
            }
        } else {
            settingsFile = new File(settingsFile.getAbsolutePath().concat(".py"));
            EventBasedTokenizerRunner.run(settingsFile, urlFileFinder);
        }

        //assert !urlFileFinder.getUrlFile().isEmpty() : "Root URL file setting does not exist.";

        if (!urlFileFinder.getUrlFile().isEmpty()) {
            rootUrlsFile = new File(rootDirectory, urlFileFinder.getUrlFile());
        }
    }

    private List<Endpoint> generateMappings(boolean i18) {
        List<Endpoint> mappings = list();
        for (DjangoRoute route : routeMap.values()) {
            String urlPath = route.getUrl();
            String filePath = route.getViewPath();

            Collection<String> httpMethods = route.getHttpMethods();
            Map<String, ParameterDataType> parameters = route.getParameters();
            mappings.add(new DjangoEndpoint(filePath, urlPath, httpMethods, parameters, false));
            if (i18) {
                mappings.add(new DjangoEndpoint(filePath, urlPath, httpMethods, parameters, true));
            }
        }
        return mappings;
    }

    private List<File> findUrlsByFileName() {
        List<File> urlFiles = list();
        Collection<File> projectFiles = FileUtils.listFiles(rootDirectory, new String[] { "py" }, true);
        for (File file : projectFiles) {
            if (file.getName().endsWith("urls.py")) {
                urlFiles.add(file);
            }
        }
        return urlFiles;
    }

    private void runInterpreterOnNonDeclarations(PythonCodeCollection codebase, PythonInterpreter interpreter) {
        for (PythonModule module : codebase.getModules()) {
            String sourcePath = module.getSourceCodePath();
            if (sourcePath == null) {
                continue;
            } else if (!new File(sourcePath).exists()) {
                continue;
            } else if (!new File(sourcePath).isFile()) {
                continue;
            }

            Collection<AbstractPythonStatement> childStatements = module.getChildStatements();

            CondensedLinesMap moduleCode = FileReadUtils.readLinesCondensed(sourcePath, 0, Integer.MAX_VALUE);
            List<String> condensedLines = moduleCode.getCondensedLines();
            Map<Integer, Boolean> allowedLineIndices = map();
            for (int i = 0; i < condensedLines.size(); i++) {
                allowedLineIndices.put(i, true);
            }

            for (AbstractPythonStatement child : childStatements) {
                if (child instanceof PythonClass || child instanceof PythonFunction) {
                    int startLine = moduleCode.getLineIndexForSourceLine(child.getSourceCodeStartLine());
                    int endLine = moduleCode.getLineIndexForSourceLine(child.getSourceCodeEndLine());
                    if (startLine >= 0 && endLine >= 0) {
                        for (int i = startLine; i <= endLine; i++) {
                            allowedLineIndices.put(i, false);
                        }
                    }
                }
            }

            for (int i = 0; i < condensedLines.size(); i++) {
                if (!allowedLineIndices.get(i)) {
                    continue;
                }

                String line = condensedLines.get(i);
                interpreter.run(line, module);
            }
        }
    }

    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return endpoints;
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return endpoints.iterator();
    }

    static class SettingsFinder implements EventBasedTokenizer {

        String settingsLocation = "";
        boolean shouldContinue = true, foundSettingsLocation = false;

        public File getSettings(String rootDirectory) { return DjangoPathCleaner.buildPath(rootDirectory, settingsLocation); }

        @Override
        public boolean shouldContinue() {
            return shouldContinue;
        }

        @Override
        public void processToken(int type, int lineNumber, String stringValue) {
            if (stringValue != null && stringValue.equals("DJANGO_SETTINGS_MODULE")) {
                foundSettingsLocation = true;
            } else if (foundSettingsLocation && stringValue != null) {
                settingsLocation = DjangoPathCleaner.cleanStringFromCode(stringValue);
            }

            if (!settingsLocation.isEmpty()) {
                shouldContinue = false;
            }
        }
    }

    static class UrlFileFinder implements EventBasedTokenizer {

        String urlFile = "";
        boolean shouldContinue = true, foundURLSetting = false;

        public String getUrlFile() { return urlFile; }

        @Override
        public boolean shouldContinue() {
            return shouldContinue;
        }

        @Override
        public void processToken(int type, int lineNumber, String stringValue) {

            if (stringValue == null) return;

            if (stringValue.equals("URLCONF")) {
                foundURLSetting = true;
            } else if (foundURLSetting) {
                urlFile = DjangoPathCleaner.cleanStringFromCode(stringValue).concat(".py");
            }

            if (!urlFile.isEmpty()) {
                shouldContinue = false;
            }
        }
    }
}
