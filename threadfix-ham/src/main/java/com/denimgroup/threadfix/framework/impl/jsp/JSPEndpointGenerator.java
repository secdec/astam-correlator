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
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.ProjectDirectory;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.NoDotDirectoryFileFilter;
import com.denimgroup.threadfix.framework.util.*;
import com.denimgroup.threadfix.framework.util.htmlParsing.ElementReference;
import com.denimgroup.threadfix.framework.util.htmlParsing.HyperlinkParameterDetector;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

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

	private JSPWebXmlConfiguration xmlConfiguration;

	private final Map<String, Set<String>> includeMap = map();
	private final Map<String, JSPEndpoint> jspEndpointMap = map();
	private final List<Endpoint> endpoints = list();
    private final ProjectDirectory projectDirectory;
	@Nullable
    private final File projectRoot, jspRoot;
	
	@SuppressWarnings("unchecked")
	public JSPEndpointGenerator(@Nonnull File rootFile) {
		if (rootFile.exists()) {

			this.projectRoot = rootFile;

            projectDirectory = new ProjectDirectory(rootFile);

            File webXmlFile = findWebXmlFile(rootFile);
            if (webXmlFile != null) {
                JSPWebXmlParser webXmlParser = new JSPWebXmlParser(webXmlFile);
                xmlConfiguration = webXmlParser.getConfiguration();
            }

            JSPServletParser servletParser = new JSPServletParser(rootFile);
			
			String jspRootString = CommonPathFinder.findOrParseProjectRootFromDirectory(rootFile, "jsp");

            LOG.info("Calculated JSP root to be: " + jspRootString);
			
			if (jspRootString == null) {
				jspRoot = projectRoot;
			} else {
				jspRoot = new File(jspRootString);
			}
			
			Collection<File> jspFiles = FileUtils.listFiles(
					rootFile, JSPFileFilter.INSTANCE, NoDotDirectoryFileFilter.INSTANCE);

            LOG.info("Found " + jspFiles.size() + " JSP files.");

			for (File file : jspFiles) {
				parseFile(file);
			}

            Collection<File> jspAndHtmlFiles = FileUtils.listFiles(rootFile, new String[] { "jsp", "html" }, true);
			List<ElementReference> elementReferences = list();
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
                List<ElementReference> parsedReferences = parameterDetector.parse(fileContents, file);
			    if (parsedReferences != null) {
			        elementReferences.addAll(parsedReferences);
                }
            }



			if (xmlConfiguration != null) {
                loadWebXmlWelcomeFiles();
                loadAnnotatedServlets(servletParser);
                loadWebXmlServletMappings(servletParser);
            }

            //detectOptionalParameters(inferredEndpointParameters);
			//mergeParsedImplicitParameters(endpoints, inferredEndpointParameters);

		} else {
            LOG.error("Root file didn't exist. Exiting.");

            projectDirectory = null;
			projectRoot = null;
			jspRoot = null;
		}
	}

	void loadWebXmlWelcomeFiles() {
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
            String relativePath = FilePathUtils.getRelativePath(welcomeFile.getAbsolutePath(), jspRoot);
            String endpointPath = relativePath.substring(0, relativePath.length() - welcomeFile.getName().length());
            JSPEndpoint welcomeEndpoint = new JSPEndpoint(relativePath, endpointPath, set("GET"), JSPParameterParser.parse(welcomeFile));
            endpoints.add(welcomeEndpoint);
            jspEndpointMap.put(relativePath, welcomeEndpoint);
        }
    }

    void loadAnnotatedServlets(JSPServletParser servletParser) {
        //  Add endpoints from servlets mapped via @WebServlet
        for (JSPServlet servlet : servletParser.getServlets()) {
            String relativeFilePath = getRelativePath(servlet.getFilePath());

            for (String endpointString : servlet.getAnnotatedEndpointBindings()) {
                JSPEndpoint newEndpoint = new JSPEndpoint(relativeFilePath, endpointString, set("GET", "POST"), servlet.getParameters());
                endpoints.add(newEndpoint);
                jspEndpointMap.put(relativeFilePath, newEndpoint);
            }
        }
    }

    void loadWebXmlServletMappings(JSPServletParser servletParser) {
        LOG.info("Found " + xmlConfiguration.getAllServletMappings().size() + " servlet mappings in web.xml.");
        for (JSPWebXmlServletMapping mapping : xmlConfiguration.getAllServletMappings()) {
            List<String> urlPatterns = mapping.getUrlPatterns();
            String filePath = null;
            Map<Integer, List<String>> parameters = null;

            switch (mapping.getMappingType()) {
                case MAP_CLASS_SERVLET:
                    String servletClass = mapping.getMappedClassServlet().getServletClass();

                    JSPServlet servlet = servletParser.findServletByAbsoluteName(servletClass);
                    if (servlet == null) {
                        LOG.info("Couldn't find Java file for servlet with class name " + servletClass);
                        continue;
                    }

                    filePath = getRelativePath(servlet.getFilePath());
                    parameters = servlet.getParameters();
                    break;

                case MAP_JSP_SERVLET:
                    JSPWebXmlJspServlet jspServlet = mapping.getMappedJspServlet();
                    filePath = getFullRelativeWebPath(jspServlet.getFilePath());
                    parameters = JSPParameterParser.parse(new File(jspServlet.getFilePath()));
                    break;

                default:
                    continue;
            }



            for (String pattern : urlPatterns) {
                JSPEndpoint endpoint = new JSPEndpoint(filePath, pattern, set("GET", "POST"), parameters);
                endpoints.add(endpoint);
                jspEndpointMap.put(filePath, endpoint);
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

        return result;
    }
	
    void parseFile(File file) {

        if (projectRoot != null) {
            // we will use both parsers on the same run through the file
            String staticPath = FilePathUtils.getRelativePath(file, projectRoot);

            JSPIncludeParser includeParser = new JSPIncludeParser(file);
            JSPParameterParser parameterParser = new JSPParameterParser();
            EventBasedTokenizerRunner.run(file, false, parameterParser, includeParser);

            addToIncludes(staticPath, includeParser.returnFiles);

            createEndpoint(staticPath, file, parameterParser.buildParametersMap());
        }
	}

    void createEndpoint(String staticPath, File file, Map<Integer, List<String>> parserResults) {
        JSPEndpoint endpoint = new JSPEndpoint(
                getInputOrEmptyString(staticPath),
                getInputOrEmptyString(FilePathUtils.getRelativePath(file, jspRoot)),
                set("GET", "POST"),
                parserResults
        );

        jspEndpointMap.put(staticPath, endpoint);

        endpoints.add(endpoint);
    }

    void addToIncludes(String staticPath, Set<File> includedFiles) {
        if (projectRoot != null && projectDirectory != null) {
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

    void addParametersFromIncludedFiles() {
        for (Map.Entry<String, JSPEndpoint> endpointEntry : jspEndpointMap.entrySet()) {
            if (endpointEntry != null && endpointEntry.getKey() != null) {
                endpointEntry.getValue().getParameters().putAll(
                        getParametersFor(endpointEntry.getKey(),
                                new HashSet<String>(), new HashMap<String, RouteParameter>()));
            }
        }
    }

    // TODO memoize results
    Map<String, RouteParameter> getParametersFor(String key, Set<String> alreadyVisited,
                                                    Map<String, RouteParameter> soFar) {

        if (alreadyVisited.contains(key)) {
            return soFar;
        }

        alreadyVisited.add(key);

        Map<String, RouteParameter> params = map();

        if (includeMap.get(key) != null) {
            for (String fileKey : includeMap.get(key)) {
                JSPEndpoint endpoint = jspEndpointMap.get(fileKey);
                if (endpoint != null) {
                    params.putAll(endpoint.getParameters());
                    params.putAll(getParametersFor(fileKey, alreadyVisited, soFar));
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
	
	public JSPEndpoint getEndpoint(String staticPath) {

        if (staticPath == null)
            return null;

		String key = staticPath;
        String keyFS = key.replace("\\","/");
		if (!keyFS.startsWith("/")) {
            keyFS = "/" + keyFS;
		}

        for (Map.Entry<String, JSPEndpoint> entry: jspEndpointMap.entrySet()) {
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
		return FilePathUtils.getRelativePath(dataFlowLocation, projectRoot);
	}

	//  Gets the path of the given web file path relative to the project path, where the web file path
    //      is relative to the WebContent root instead of project root
	String getFullRelativeWebPath(String localRelativePath) {
	    String fullPath = jspRoot.getAbsolutePath();
	    if (fullPath.charAt(fullPath.length() - 1) == '/') {
	        fullPath = fullPath.substring(0, fullPath.length() - 1);
        }

        if (localRelativePath.length() > 0 && localRelativePath.charAt(0) == '/') {
	        localRelativePath = localRelativePath.substring(1);
        }

        fullPath += "/" + localRelativePath;

	    return getRelativePath(fullPath);
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
