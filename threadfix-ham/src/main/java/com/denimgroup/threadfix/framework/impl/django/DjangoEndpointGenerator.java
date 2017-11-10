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
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
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

    public DjangoEndpointGenerator(@Nonnull File rootDirectory) {
        assert rootDirectory.exists() : "Root file did not exist.";
        assert rootDirectory.isDirectory() : "Root file was not a directory.";

        this.rootDirectory = rootDirectory;

        findRootUrlsFile();
        if (rootUrlsFile == null || !rootUrlsFile.exists()) {
            possibleGuessedUrlFiles = findUrlsByFileName();
        }

        boolean foundUrlFiles = (rootUrlsFile == null || !rootUrlsFile.exists()) && (possibleGuessedUrlFiles == null || possibleGuessedUrlFiles.size() == 0);
        assert foundUrlFiles : "Root URL file did not exist";

        PythonCodeCollection classes = PythonSyntaxParser.run(rootDirectory);

        if (rootUrlsFile != null && rootUrlsFile.exists()) {
            routeMap = DjangoRouteParser.parse(rootDirectory.getAbsolutePath(), "", classes, rootUrlsFile);
        } else if (possibleGuessedUrlFiles != null && possibleGuessedUrlFiles.size() > 0) {

            LOG.debug("Found " + possibleGuessedUrlFiles.size() + " possible URL files:");
            for (File urlFile : possibleGuessedUrlFiles) {
                LOG.debug("- " + urlFile.getAbsolutePath());
            }

            routeMap = map();
            for (File guessedUrlsFile : possibleGuessedUrlFiles) {
                Map<String, DjangoRoute> guessedUrls = DjangoRouteParser.parse(rootDirectory.getAbsolutePath(), "", classes, guessedUrlsFile);
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

        this.endpoints = generateMappings();
    }

    private void findRootUrlsFile() {
        File manageFile = new File(rootDirectory, "manage.py");
        assert manageFile.exists() : "manage.py does not exist in root directory";
        SettingsFinder settingsFinder = new SettingsFinder();
        EventBasedTokenizerRunner.run(manageFile, settingsFinder);

        File settingsFile = settingsFinder.getSettings(rootDirectory.getPath());
        assert settingsFile.exists() : "Settings file not found";
        UrlFileFinder urlFileFinder = new UrlFileFinder();

        if (settingsFile.isDirectory()) {
            for (File file : settingsFile.listFiles()) {
                EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, urlFileFinder);
                if (!urlFileFinder.shouldContinue()) break;
            }
        } else {
            settingsFile = new File(settingsFile.getAbsolutePath().concat(".py"));
            EventBasedTokenizerRunner.run(settingsFile, urlFileFinder);
        }
        assert !urlFileFinder.getUrlFile().isEmpty() : "Root URL file setting does not exist.";

        if (!urlFileFinder.getUrlFile().isEmpty()) {
            rootUrlsFile = new File(rootDirectory, urlFileFinder.getUrlFile());
        }
    }

    private List<Endpoint> generateMappings() {
        List<Endpoint> mappings = list();
        for (DjangoRoute route : routeMap.values()) {
            String urlPath = route.getUrl();
            String filePath = route.getViewPath();

            Collection<String> httpMethods = route.getHttpMethods();
            Map<String, ParameterDataType> parameters = route.getParameters();
            mappings.add(new DjangoEndpoint(filePath, urlPath, httpMethods, parameters));
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
