////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.ProjectDirectory;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.NoDotDirectoryFileFilter;
import com.denimgroup.threadfix.framework.util.*;
import com.denimgroup.threadfix.framework.util.htmlParsing.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

// TODO figure out HTTP methods perhaps from form analysis
public class JSPEndpointGenerator implements EndpointGenerator {

    private static final SanitizedLogger LOG = new SanitizedLogger(JSPEndpointGenerator.class);

    private final Map<String, List<JSPEndpoint>> jspEndpointMap = map();
    private final List<Endpoint> endpoints = list();
    private final ProjectDirectory projectDirectory;
    @Nullable
    private File rootFile;

    @SuppressWarnings("unchecked")
    public JSPEndpointGenerator(@Nonnull File rootFile) {
        if (rootFile.exists()) {

            this.rootFile = rootFile;

            projectDirectory = new ProjectDirectory(rootFile);

            List<File> projectFolders = findProjectFolders(rootFile);

            for (File projectFolder : projectFolders) {

                File webXmlFile = findWebXmlFile(projectFolder);
                JSPWebXmlConfiguration xmlConfiguration = null;
                if (webXmlFile != null) {
                    JSPWebXmlParser webXmlParser = new JSPWebXmlParser(webXmlFile);
                    xmlConfiguration = webXmlParser.getConfiguration();
                }

                JSPServletParser servletParser = new JSPServletParser(projectFolder);

                String jspRootString = CommonPathFinder.findOrParseProjectRootFromDirectory(projectFolder, "jsp");

                LOG.info("Calculated JSP root to be: " + jspRootString);

                File jspRoot;

                if (jspRootString == null) {
                    jspRoot = projectFolder;
                } else {
                    File possibleRoot = new File(jspRootString);
                    if (!possibleRoot.isDirectory()) {
                        jspRootString = jspRootString.substring(0, jspRootString.lastIndexOf('/'));
                        jspRoot = new File(jspRootString);
                    } else {
                        jspRoot = possibleRoot;
                    }
                }

                Collection<File> jspFiles = FileUtils.listFiles(jspRoot, JSPFileFilter.INSTANCE, NoDotDirectoryFileFilter.INSTANCE);
	            List<Endpoint> projectEndpoints = list();
	            Map<String, Set<String>> includeMap = map();

                LOG.info("Found " + jspFiles.size() + " JSP files.");

                for (File file : jspFiles) {
                    if (!file.getAbsolutePath().toLowerCase().contains("web-inf")) {
                        parseFile(projectEndpoints, includeMap, jspRoot, file);
                    }
                }

                Collection<File> jspAndHtmlFiles = FileUtils.listFiles(projectFolder, new String[]{"jsp", "html"}, true);
                List<HyperlinkParameterDetectionResult> implicitParams = list();
                for (File file : jspAndHtmlFiles) {
                    HyperlinkParameterDetector parameterDetector = new HyperlinkParameterDetector();
                    String fileContents;
                    try {
                        fileContents = FileUtils.readFileToString(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    fileContents = stripJspElements(fileContents);
                    HyperlinkParameterDetectionResult parsedReferences = parameterDetector.parse(fileContents, file);
                    if (parsedReferences != null) {
                        implicitParams.add(parsedReferences);
                    }
                }

                if (xmlConfiguration != null) {
                    loadWebXmlWelcomeFiles(projectEndpoints, xmlConfiguration, jspRoot);
                    loadAnnotatedServlets(projectEndpoints, servletParser);
                    loadWebXmlServletMappings(projectEndpoints, xmlConfiguration, jspRoot, servletParser);
                }

	            addParametersFromIncludedFiles(includeMap, projectEndpoints);
                updateFileParameters(projectEndpoints, implicitParams);

                int numAddedParams = 0, numRemovedParams = 0;

                HyperlinkParameterMerger parameterMerger = new HyperlinkParameterMerger(true, true);
                for (HyperlinkParameterDetectionResult params : implicitParams) {
                    HyperlinkParameterMergingGuide mergeGuide = parameterMerger.mergeParsedImplicitParameters(projectEndpoints, params);

                    for (Endpoint endpoint : projectEndpoints) {
                        JSPEndpoint jspEndpoint = (JSPEndpoint) endpoint;
                        List<RouteParameter> addedParams = mergeGuide.findAddedParameters(endpoint, endpoint.getHttpMethod());
                        if (addedParams != null) {
                            for (RouteParameter newParam : addedParams) {
                                jspEndpoint.getParameters().put(newParam.getName(), newParam);
                                ++numAddedParams;
                            }
                        }

                        List<RouteParameter> removedParams = mergeGuide.findRemovedParameters(endpoint, endpoint.getHttpMethod());
                        if (removedParams != null) {
                            for (RouteParameter oldParam : removedParams) {
                                jspEndpoint.getParameters().remove(oldParam.getName());
                                ++numRemovedParams;
                            }
                        }
                    }
                }

                LOG.info("Detected " + numAddedParams + " new parameters and removed " + numRemovedParams + " misassigned parameters after HTML reference parsing");

                ParameterMerger genericMerger = new ParameterMerger();
                genericMerger.setCaseSensitive(true);
                Map<Endpoint, Map<String, RouteParameter>> mergedParams = genericMerger.mergeParametersIn(projectEndpoints);

                for (Endpoint endpoint : mergedParams.keySet()) {
                    JSPEndpoint jspEndpoint = (JSPEndpoint) endpoint;
                    Map<String, RouteParameter> replacedParameters = mergedParams.get(endpoint);
                    for (String paramName : replacedParameters.keySet()) {
                        jspEndpoint.getParameters().put(paramName, replacedParameters.get(paramName));
                    }
                }

                this.endpoints.addAll(projectEndpoints);
            }

            applyLineNumbers(endpoints);

            EndpointUtil.rectifyVariantHierarchy(endpoints);

            EndpointValidationStatistics.printValidationStats(endpoints);

        } else {
            LOG.error("Root file didn't exist. Exiting.");

            projectDirectory = null;
            this.rootFile = null;
        }
    }

    void updateFileParameters(List<Endpoint> endpoints, List<HyperlinkParameterDetectionResult> parameterDetectionResults) {
        // Update endpoints with 'FILES' parameters to have the proper name (they're set to "[File]" by default)
        Queue<ElementReference> pendingElements = new ArrayDeque<ElementReference>();
        for (HyperlinkParameterDetectionResult detection : parameterDetectionResults) {
            pendingElements.addAll(detection.getAllSourceReferences());
        }

        while (pendingElements.size() > 0) {
            ElementReference nextElement = pendingElements.remove();

            if (!nextElement.getElementType().equals("input")) {
                continue;
            }

            String inputType = nextElement.getAttributeValue("type");
            if (inputType == null || inputType.isEmpty() || !inputType.equalsIgnoreCase("file")) {
                continue;
            }

            String inputName = nextElement.getAttributeValue("name");
            if (inputName == null || inputName.isEmpty()) {
                continue;
            }

            String url = null;
            ElementReference parent = nextElement.getParent();
            while (parent != null && url == null) {
                url = parent.getTargetEndpoint();
                if (url == null) {
                    parent = parent.getParent();
                }
            }

            if (url == null) {
                continue;
            }

            Endpoint baseEndpoint = findBestEndpoint(url, endpoints);
            List<Endpoint> relevantEndpoints = list();
            for (Endpoint endpoint : endpoints) {
                if (areEndpointsAliased(baseEndpoint, endpoint)) {
                    relevantEndpoints.add(endpoint);
                }
            }

            for (Endpoint endpoint : relevantEndpoints) {
                if (!endpoint.getHttpMethod().equalsIgnoreCase(parent.getDefaultRequestType())) {
                    continue;
                }

                List<String> oldParams = list();
                List<RouteParameter> newParams = list();
                boolean updatedFileParam = false;
                for (String paramName : endpoint.getParameters().keySet()) {
                    RouteParameter param = endpoint.getParameters().get(paramName);
                    if (param.getParamType() != RouteParameterType.FILES) {
                        continue;
                    }

                    if (!paramName.equalsIgnoreCase(inputName)) {
                        param.setName(inputName);
                        oldParams.add(paramName);
                        newParams.add(param);
                        updatedFileParam = true;
                    }
                }

                if (!updatedFileParam) {
                    RouteParameter newFileParam = new RouteParameter(inputName);
                    newFileParam.setParamType(RouteParameterType.FILES);
                    newFileParam.setDataType("String");
                    endpoint.getParameters().put(inputName, newFileParam);
                }
            }
        }
    }

    boolean areEndpointsAliased(Endpoint a, Endpoint b) {
        String urlA = a.getUrlPath();
        String urlB = b.getUrlPath();

        urlA = CodeParseUtil.trim(urlA, "/");
        urlB = CodeParseUtil.trim(urlB, "/");

        return
                StringUtils.countMatches(urlA, "/") == StringUtils.countMatches( urlB, "/") &&
                        a.getStartingLineNumber() == b.getStartingLineNumber() &&
                        a.getFilePath().equalsIgnoreCase(b.getFilePath());
    }

    Endpoint findBestEndpoint(String reference, List<Endpoint> endpoints) {
        int bestScore = -10000;
        Endpoint bestMatch = null;
        for (Endpoint endpoint : endpoints) {
            int score = endpoint.compareRelevance(reference);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = endpoint;
            }
        }
        return bestMatch;
    }

    void applyLineNumbers(Collection<Endpoint> endpoints) {
        Collection<Endpoint> allEndpoints = EndpointUtil.flattenWithVariants(endpoints);

        for (Endpoint endpoint : allEndpoints) {
            String filePath = endpoint.getFilePath();
            File file = new File(filePath);
            if (!file.isAbsolute() || !file.exists()) {
                filePath = PathUtil.combine(rootFile.getAbsolutePath(), filePath);
                file = new File(filePath);
            }

            if (!file.exists()) {
            	continue;
            }

            if (endpoint.getStartingLineNumber() > 0 && endpoint.getEndingLineNumber() > 0) {
            	continue;
            }

	        JSPEndpoint jspEndpoint = (JSPEndpoint) endpoint;

	        if (filePath.endsWith(".jsp")) {
		        int numLines = CodeParseUtil.countLines(file.getAbsolutePath());
		        jspEndpoint.setLines(1, numLines + 1);
	        }
        }
    }

    void loadWebXmlWelcomeFiles(List<Endpoint> endpoints, JSPWebXmlConfiguration xmlConfiguration, File jspRoot) {
        List<File> welcomeFileLocations = list();
        for (File discoveredFile : FileUtils.listFiles(jspRoot, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            String fileName = discoveredFile.getName();
            for (String welcomeFileName : xmlConfiguration.getWelcomeFileList()) {
                if (fileName.equalsIgnoreCase(welcomeFileName)) {
                    welcomeFileLocations.add(discoveredFile);
                    break;
                }
            }
        }

        for (File welcomeFile : welcomeFileLocations) {
            String relativePath = FilePathUtils.getRelativePath(welcomeFile.getAbsolutePath(), rootFile);
            String webRelativePath = FilePathUtils.getRelativePath(welcomeFile.getAbsolutePath(), jspRoot);
            String endpointPath = webRelativePath.substring(0, webRelativePath.length() - welcomeFile.getName().length());
            JSPEndpoint welcomeEndpoint = new JSPEndpoint(relativePath, endpointPath, "GET", JSPParameterParser.parse(welcomeFile));
            endpoints.add(welcomeEndpoint);
            addToEndpointMap(relativePath, welcomeEndpoint);
        }
    }

    void loadAnnotatedServlets(List<Endpoint> endpoints, JSPServletParser servletParser) {
        //  Add endpoints from servlets mapped via @WebServlet
        for (JSPServlet servlet : servletParser.getServlets()) {
            String relativeFilePath = getRelativePath(servlet.getFilePath());

            Set<String> servletMethods = servlet.getHttpMethods();
            for (String endpointPath : servlet.getAnnotatedEndpoints()) {
                for (String method : servletMethods) {
                    List<RouteParameter> params = servlet.getMethodParameters(method);
                    Map<String, RouteParameter> paramMap = map();
                    for (RouteParameter param : params) {
                        paramMap.put(param.getName(), param);
                    }
                    int startLine = servlet.getStartLine(method);
                    int endLine = servlet.getEndLine(method);
                    JSPEndpoint newEndpoint = new JSPEndpoint(relativeFilePath, endpointPath, method, paramMap);
                    newEndpoint.setLines(startLine, endLine);
                    endpoints.add(newEndpoint);
                    addToEndpointMap(relativeFilePath, newEndpoint);
                }
            }
        }
    }

    void loadWebXmlServletMappings(List<Endpoint> endpoints, JSPWebXmlConfiguration xmlConfiguration, File jspRoot, JSPServletParser servletParser) {
        LOG.info("Found " + xmlConfiguration.getAllServletMappings().size() + " servlet mappings in web.xml.");
        for (JSPWebXmlServletMapping mapping : xmlConfiguration.getAllServletMappings()) {
            List<String> urlPatterns = mapping.getUrlPatterns();
            String filePath = null;
            Map<String, Map<String, RouteParameter>> methodParameters = null;
            Collection<String> supportedMethods = null;
            Map<String, Integer> methodStartLines = map();
            Map<String, Integer> methodEndLines = map();

            switch (mapping.getMappingType()) {
                case MAP_CLASS_SERVLET:
                    String servletClass = mapping.getMappedClassServlet().getServletClass();

                    JSPServlet servlet = servletParser.findServletByAbsoluteName(servletClass);
                    if (servlet == null) {
                        LOG.info("Couldn't find Java file for servlet with class name " + servletClass);
                        continue;
                    }

                    filePath = getRelativePath(servlet.getFilePath());
                    methodParameters = map();
                    supportedMethods = servlet.getHttpMethods();

                    for (String method : supportedMethods) {
                    	methodStartLines.put(method, servlet.getStartLine(method));
                    	methodEndLines.put(method, servlet.getEndLine(method));
                        Map<String, RouteParameter> currentMap = map();
                        for (RouteParameter param : servlet.getMethodParameters(method)) {
                            currentMap.put(param.getName(), param);
                        }
                        methodParameters.put(method, currentMap);
                    }
                    break;

                case MAP_JSP_SERVLET:
                    JSPWebXmlJspServlet jspServlet = mapping.getMappedJspServlet();
                    String absolutePath = jspServlet.getFilePath();
                    File absolutePathFile = new File(absolutePath);
                    if (!absolutePathFile.isAbsolute() || !absolutePathFile.exists()) {
                        absolutePath = PathUtil.combine(jspRoot.getAbsolutePath(), jspServlet.getFilePath());
                    }
                    filePath = getRelativePath(absolutePath);
                    Map<String, RouteParameter> jspParameters = JSPParameterParser.parse(new File(absolutePath));
                    methodParameters = map();
                    supportedMethods = list("GET", "POST"); // Can't determine whether GET or POST is required, emit both
                    for (String method : supportedMethods) {
                        methodParameters.put(method, jspParameters);
                    }
                    break;

                default:
                    continue;
            }

            Map<String, JSPEndpoint> primaryMethodEndpoints = map();

            for (String pattern : urlPatterns) {
                for (String httpMethod : supportedMethods) {
                    JSPEndpoint newEndpoint = new JSPEndpoint(filePath, pattern, httpMethod, methodParameters.get(httpMethod));
                    if (methodStartLines.containsKey(httpMethod) && methodEndLines.containsKey(httpMethod)) {
                    	newEndpoint.setLines(methodStartLines.get(httpMethod), methodEndLines.get(httpMethod));
                    }
                    if (!primaryMethodEndpoints.containsKey(httpMethod)) {
                        endpoints.add(newEndpoint);
                        primaryMethodEndpoints.put(httpMethod, newEndpoint);
                    } else {
                        primaryMethodEndpoints.get(httpMethod).addVariant(newEndpoint);
                    }
                    addToEndpointMap(filePath, newEndpoint);
                }
            }
        }
    }

    File findWebXmlFile(File startingDirectory) {
        File result = null;
        if (!startingDirectory.isDirectory()) {
            return result;
        }

        long largestFileSize = -1;

        for (File file : startingDirectory.listFiles()) {
            if (file.isFile()) {
                if (file.getName().equalsIgnoreCase("web.xml")) {
                    long fileSize = file.length();
                    if (fileSize > largestFileSize) {
                        result = file;
                        largestFileSize = fileSize;
                    }
                }
            } else {
                if (!file.getName().equalsIgnoreCase("target") && !file.getName().equalsIgnoreCase("out")) {
                    File subFile = findWebXmlFile(file);
                    if (subFile != null) {
                        long fileSize = subFile.length();
                        if (fileSize > largestFileSize) {
                            result = subFile;
                            largestFileSize = fileSize;
                        }
                    }
                }
            }
        }

        return result;
    }

    void parseFile(List<Endpoint> outputCollection, Map<String, Set<String>> includeMap, File jspRoot, File file) {

        if (rootFile != null) {
            // we will use both parsers on the same run through the file
            String staticPath = FilePathUtils.getRelativePath(file, rootFile);

            JSPIncludeParser includeParser = new JSPIncludeParser(file);
            JSPParameterParser parameterParser = new JSPParameterParser();
            EventBasedTokenizerRunner.run(file, false, parameterParser, includeParser);

            addToIncludes(includeMap, staticPath, includeParser.returnFiles);

            createEndpoint(outputCollection, jspRoot, staticPath, file, parameterParser.buildParametersMap());
        }
    }

    void createEndpoint(List<Endpoint> targetEndpoints, File jspRoot, String staticPath, File file, Map<String, RouteParameter> parserResults) {
        staticPath = getInputOrEmptyString(staticPath);
        String endpointPath = getInputOrEmptyString(FilePathUtils.getRelativePath(file, jspRoot));

        JSPEndpoint primaryEndpoint = new JSPEndpoint(staticPath, endpointPath, "GET", parserResults);
        addToEndpointMap(staticPath, primaryEndpoint);
        targetEndpoints.add(primaryEndpoint);

        JSPEndpoint subEndpoint = new JSPEndpoint(staticPath, endpointPath, "POST", parserResults);
        primaryEndpoint.addVariant(subEndpoint);
        addToEndpointMap(staticPath, subEndpoint);
        //endpoints.add(endpoint);
    }

    void addToIncludes(Map<String, Set<String>> includeMap, String staticPath, Set<File> includedFiles) {
        if (rootFile != null && projectDirectory != null) {
            if (!includedFiles.isEmpty()) {
                Set<String> cleanedFilePaths = set();

                for (File file : includedFiles) {
                    String cleaned = projectDirectory.findCanonicalFilePath(file);
                    if (cleaned != null) {
                        cleanedFilePaths.add(cleaned);
                    }
                }

                includeMap.put(staticPath, cleanedFilePaths);
            }
        }
    }

    void addToEndpointMap(String filePath, JSPEndpoint endpoint) {
        List<JSPEndpoint> endpoints = jspEndpointMap.get(filePath);
        if (endpoints == null) {
            jspEndpointMap.put(filePath, endpoints = list());
        }
        endpoints.add(endpoint);
    }

    void addParametersFromIncludedFiles(Map<String, Set<String>> includeMap, List<Endpoint> endpoints) {
    	for (Endpoint endpoint : endpoints) {
    		JSPEndpoint jspEndpoint = (JSPEndpoint)endpoint;
		    endpoint.getParameters().putAll(
				    getParametersFor(includeMap, endpoint.getFilePath(),
						    new HashSet<String>(), new HashMap<String, RouteParameter>()));
	    }
    }

    // TODO memoize results
    Map<String, RouteParameter> getParametersFor(Map<String, Set<String>> includeMap, String key, Set<String> alreadyVisited,
                                                    Map<String, RouteParameter> soFar) {

        if (alreadyVisited.contains(key)) {
            return soFar;
        }

        alreadyVisited.add(key);

        Map<String, RouteParameter> params = map();

        if (includeMap.get(key) != null) {
            for (String fileKey : includeMap.get(key)) {
                List<JSPEndpoint> endpoints = jspEndpointMap.get(fileKey);
                if (endpoints != null) {
                    for (JSPEndpoint endpoint : endpoints) {
                        params.putAll(endpoint.getParameters());
                        params.putAll(getParametersFor(includeMap, fileKey, alreadyVisited, soFar));
                    }
                }
            }
        }

        return params;
    }

    private String stripJspElements(String jspFileContents) {
        return Pattern.compile("<%((?!%>).)*%>", Pattern.DOTALL).matcher(jspFileContents).replaceAll("");
    }

    @Nonnull
    private String getInputOrEmptyString(@Nullable String input) {
        return input == null ? "" : input;
    }

    public List<JSPEndpoint> getEndpoints(String staticPath) {

        if (staticPath == null)
            return null;

        String key = staticPath;
        String keyFS = key.replace("\\","/");
        if (!keyFS.startsWith("/")) {
            keyFS = "/" + keyFS;
        }

        for (Map.Entry<String, List<JSPEndpoint>> entry: jspEndpointMap.entrySet()) {
            String keyEntry = entry.getKey();
            String keyEntryFS = keyEntry.replace("\\","/");

            if ((keyEntry.isEmpty() && !key.isEmpty())
                    || (key.isEmpty() && !keyEntry.isEmpty()))
                continue;

            if (keyEntryFS.endsWith(keyFS) || keyFS.endsWith(keyEntryFS))
                return entry.getValue();
        }

        return null;
    }

    public String getRelativePath(String dataFlowLocation) {
        return FilePathUtils.getRelativePath(dataFlowLocation, rootFile);
    }

    private List<File> findProjectFolders(File rootFolder) {
        List<File> webXmlFolders = list();
        for (File xmlFile : FileUtils.listFiles(rootFolder, new String[] { "xml" }, true)) {
            if (xmlFile.getName().equalsIgnoreCase("web.xml")) {
                webXmlFolders.add(xmlFile.getParentFile());
            }
        }

        // If no web.xml files were found (or only one was found) treat the whole folder as the root directory
        if (webXmlFolders.size() < 2) {
        	return list(rootFolder);
        }

        List<File> distinctWebXmlFolders = FilePathUtils.findRootFolders(webXmlFolders);
        //  Traverse distinct folders to their parents

        List<File> projectFolders = list();

        for (File main : distinctWebXmlFolders) {
            String longestCommonSubstring = null;

            for (File other : distinctWebXmlFolders) {
                if (other.equals(main)) {
                    continue;
                }

                String commonSubstring = findCommonPath(main.getAbsolutePath(), other.getAbsolutePath());
                if (longestCommonSubstring == null || commonSubstring.length() > longestCommonSubstring.length())
                    longestCommonSubstring = commonSubstring;
            }

            if (longestCommonSubstring != null) {
                String uncommonPart = main.getAbsolutePath().substring(longestCommonSubstring.length() + 1);
                String uncommonFolder = uncommonPart.split("[\\/\\\\]")[0];
                projectFolders.add(new File(longestCommonSubstring, uncommonFolder));
            } else {
                projectFolders.add(main);
            }
        }

        return projectFolders;
    }

    private String findCommonPath(String pathA, String pathB) {
        String[] aParts = pathA.split("[\\/\\\\]");
        String[] bParts = pathB.split("[\\/\\\\]");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < aParts.length && i < bParts.length; i++) {
            if (!aParts[i].equals(bParts[i]))
                break;

            if (sb.length() > 0) {
                sb.append(File.separatorChar);
            }

            sb.append(aParts[i]);
        }

        return sb.toString();
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
}
