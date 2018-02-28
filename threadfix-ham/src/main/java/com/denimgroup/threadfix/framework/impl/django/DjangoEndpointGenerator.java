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

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.DjangoApiConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
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
    private Map<String, List<DjangoRoute>> routeMap;

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

        this.rootDirectory = rootDirectory.getAbsoluteFile();

        findRootUrlsFile();
        if (rootUrlsFile == null || !rootUrlsFile.exists()) {
            possibleGuessedUrlFiles = findUrlsByFileName();
        }

        boolean foundUrlFiles = (rootUrlsFile != null && rootUrlsFile.exists()) || (possibleGuessedUrlFiles != null && possibleGuessedUrlFiles.size() > 0);
        assert foundUrlFiles : "Root URL file did not exist";

        long startupStartTime = System.currentTimeMillis();

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

        debugLog("Initializing codebase before attaching Django APIs...");
        codebase.initialize();

        DjangoProject project = DjangoProject.loadFrom(rootDirectory, codebase);
        DjangoApiConfigurator djangoApis = new DjangoApiConfigurator(project);

        debugLog("Attaching known Django APIs");
        djangoApis.applySchema(codebase);

        debugLog("Re-initializing codebase...");
        codebase.initialize();

        djangoApis.applySchemaPostLink(codebase);

        LOG.info("Finished initializing codebase final entries are: "
                + codebase.getModules().size() + " modules, "
                + codebase.getClasses().size() + " classes, "
                + codebase.getFunctions().size() + " functions, "
                + codebase.getPublicVariables().size() + " public variables, "
                + codebase.get(PythonVariableModification.class).size() + " variable changes, and "
                + codebase.get(PythonFunctionCall.class).size() + " function calls.");

        //PythonDebugUtil.printDuplicateStatements(codebase);

        debugLog("Preparing Python interpreter...");

        LOG.info("Executing module-level code...");
        PythonInterpreter interpreter = new PythonInterpreter(codebase);

        long interpreterStartTime = System.currentTimeMillis();

        djangoApis.applyRuntime(interpreter);
        runInterpreterOnNonDeclarations(codebase, interpreter);

        long interpreterDuration = System.currentTimeMillis() - interpreterStartTime;
        LOG.info("Running Python Interpreter finished in " + interpreterDuration + "ms");

        long startupDuration = System.currentTimeMillis() - startupStartTime;
        LOG.info("Initialization of Python metadata took " + startupDuration + "ms");

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
                Map<String, List<DjangoRoute>> guessedUrls = DjangoRouteParser.parse(
                        rootDirectory.getAbsolutePath(),
                        "",
                        guessedUrlsFile.getAbsolutePath(),
                        codebase,interpreter,
                        guessedUrlsFile);

                for (Map.Entry<String, List<DjangoRoute>> url : guessedUrls.entrySet()) {
                    List<DjangoRoute> existingRoutes = routeMap.get(url.getKey());
                    if (existingRoutes != null) {
                        existingRoutes.addAll(url.getValue());
                    } else {
                        routeMap.put(url.getKey(), url.getValue());
                    }
                }
            }
        } else {
            routeMap = map();
        }

        this.endpoints = generateMappings(i18Detector.isLocalized());

        //  Ensure that all file paths are relative to project root
        //  Python interpreter requires that file paths be absolute so that the files can be loaded
        for (Endpoint endpoint : this.endpoints) {
            DjangoEndpoint djangoEndpoint = (DjangoEndpoint)endpoint;
            String filePath = djangoEndpoint.getFilePath();
            if (filePath.startsWith(rootDirectory.getAbsolutePath())) {
                String relativePath = FilePathUtils.getRelativePath(filePath, rootDirectory);
                djangoEndpoint.setFilePath(relativePath);
            }
        }

        EndpointUtil.rectifyVariantHierarchy(endpoints);

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
        for (List<DjangoRoute> routeSet : routeMap.values()) {
            for (DjangoRoute route : routeSet) {
                String urlPath = route.getUrl();
                String filePath = route.getViewPath();

                String httpMethod = route.getHttpMethod();
                Map<String, RouteParameter> parameters = route.getParameters();
                DjangoEndpoint primaryEndpoint = new DjangoEndpoint(filePath, urlPath, httpMethod, parameters, false);
                mappings.add(primaryEndpoint);
                if (i18) {
                    primaryEndpoint.addVariant(new DjangoEndpoint(filePath, urlPath, httpMethod, parameters, true));
                }
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

            PythonSourceReader sourceReader = new PythonSourceReader(new File(sourcePath), true);
            sourceReader.ignoreChildren(module, PythonClass.class, PythonFunction.class);

            List<String> lines = sourceReader.getLines();
            for (String line : lines) {
                interpreter.run(line, module, null);
            }
        }
    }

    private void generateParameters(PythonCodeCollection codebase, Collection<Endpoint> endpoints) {
        for (Endpoint tfxEndpoint : endpoints) {
            DjangoEndpoint endpoint = (DjangoEndpoint)tfxEndpoint;
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
