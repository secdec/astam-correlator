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
package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.data.entities.Document;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.util.EndpointUtil;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by mac on 9/4/14.
 */
public class WebFormsEndpointGenerator implements EndpointGenerator {

    private static SanitizedLogger LOG = new SanitizedLogger(WebFormsEndpointGenerator.class);

    private List<Endpoint> endpoints = list();

    public WebFormsEndpointGenerator(@Nonnull File rootDirectory) {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory passed to WebFormsEndpointGenerator: " + rootDirectory);
        }

        LOG.debug("Detecting projects in " + rootDirectory.getAbsolutePath());
        List<File> projectDirectories = findProjectDirectories(rootDirectory);
        LOG.debug("Detected " + projectDirectories.size() + " projects");

        Map<String, AscxFile> ascxFiles = map();

        // Collect ASCX controls across all projects
        for (File projectDirectory : projectDirectories) {
            ascxFiles.putAll(AscxFileMappingsFileParser.getMap(projectDirectory));
        }

        for (File projectDirectory : projectDirectories) {
            File webConfig = getWebConfigFile(projectDirectory);
            Map<String, AspxParser> masterFileMap = MasterPageParser.getMasterFileMap(projectDirectory, ascxFiles);

            List<AspxParser> aspxParsers = getAspxParsers(projectDirectory, ascxFiles, masterFileMap);
            List<AspxCsParser> aspxCsParsers = getAspxCsParsers(projectDirectory);

            List<String> defaultPages = collectDefaultPages(webConfig);
            collapseToEndpoints(aspxCsParsers, aspxParsers, rootDirectory, projectDirectory, defaultPages);
        }

        //  There's currently no way to distinguish HTTP methods for each route, nor for
        //  determining which parameters go to which HTTP method. For now, just make a copy
        //  of each endpoint and set the copy to have a POST method.

        //  Duplicate the array so we don't get modify-during-iteration errors
        for (Endpoint endpoint : new ArrayList<Endpoint>(endpoints)) {
            WebFormsEndpointBase formsEndpoint = (WebFormsEndpointBase)endpoint;
            WebFormsEndpointBase duplicateEndpoint = formsEndpoint.duplicate();
            duplicateEndpoint.setHttpMethod("POST");
            endpoints.add(duplicateEndpoint);

            //  Duplicate variants as well
            for (Endpoint variant : new ArrayList<Endpoint>(endpoint.getVariants())) {
                WebFormsEndpointBase formsVariant = (WebFormsEndpointBase)variant;
                WebFormsEndpointBase duplicateVariant = formsVariant.duplicate();

                duplicateVariant.setHttpMethod("POST");
                duplicateEndpoint.addVariant(duplicateVariant);
            }
        }

        EndpointUtil.rectifyVariantHierarchy(endpoints);
    }

    private List<File> findProjectDirectories(File rootDirectory) {
        Collection<File> csprojFiles = FileUtils.listFiles(rootDirectory, new String[] { "csproj" }, true);
        List<File> result = list();
        for (File csproj : csprojFiles) {
            File folder = csproj.getParentFile();
            if (!FileUtils.listFiles(folder, new String[] { "aspx", "ascx", "asax" }, true).isEmpty()) {
                result.add(folder);
            }
        }
        return result;
    }

    private File getWebConfigFile(File rootDirectory) {
        File webConfigFile = null;
        File[] rootFiles = rootDirectory.listFiles();

        for (File file : rootFiles) {
            if (file.getName().equalsIgnoreCase("web.config")) {
                webConfigFile = file;
            } else if (file.isDirectory()) {
                webConfigFile = getWebConfigFile(file);
            }

            if (webConfigFile != null)
                break;
        }

        return webConfigFile;
    }

    private List<AspxCsParser> getAspxCsParsers(File rootDirectory) {
        Collection aspxCsFiles = FileUtils.listFiles(rootDirectory,
                new FileExtensionFileFilter(".cs"), TrueFileFilter.INSTANCE);

        List<AspxCsParser> aspxCsParsers = list();

        for (Object aspxCsFile : aspxCsFiles) {
            if (aspxCsFile instanceof File) {
                aspxCsParsers.add(AspxCsParser.parse((File) aspxCsFile));
            }
        }

        return aspxCsParsers;
    }

    private List<AspxParser> getAspxParsers(File rootDirectory,
                                            Map<String, AscxFile> map,
                                            Map<String, AspxParser> masterFileMap) {
        Collection aspxFiles = FileUtils.listFiles(rootDirectory,
                new FileExtensionFileFilter("aspx"), TrueFileFilter.INSTANCE);

        List<AspxParser> aspxParsers = list();

        for (Object aspxFile : aspxFiles) {
            if (aspxFile instanceof File) {
                File file = (File) aspxFile;

                AspxParser aspxParser = AspxParser.parse(file);
                AspxUniqueIdParser uniqueIdParser = AspxUniqueIdParser.parse(file, map);

                if (masterFileMap.containsKey(uniqueIdParser.masterPage)) {
                    aspxParser.parameters.addAll(masterFileMap.get(uniqueIdParser.masterPage).parameters);
                }

                aspxParser.parameters.addAll(uniqueIdParser.parameters);
                aspxParsers.add(aspxParser);
            }
        }
        return aspxParsers;
    }

    List<String> collectDefaultPages(File webConfig) {

        List<String> result = list();
        result.add("Default.aspx");
        result.add("Default.asp");

        if (webConfig == null) {
            return result;
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        org.w3c.dom.Document webConfigXml;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return result;
        }

        try {
            webConfigXml = documentBuilder.parse(webConfig);
        } catch (SAXException e) {
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        NodeList documentNodes = webConfigXml.getElementsByTagName("defaultDocument");
        if (documentNodes == null || documentNodes.getLength() == 0) {
            return result;
        }

        Node documentNode = documentNodes.item(0);
        NodeList fileNodes;

        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            String searchQuery = "/configuration/system.webServer/defaultDocument/files";
            fileNodes = (NodeList)xPath.compile(searchQuery).evaluate(webConfigXml, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return result;
        }

        if (fileNodes == null || fileNodes.getLength() == 0) {
            return result;
        }

        for (int i = 0; i < fileNodes.getLength(); i++) {
            Node fileNode = fileNodes.item(i);

            Node child = fileNode.getFirstChild();
            while (child != null) {

                String nodeName = child.getNodeName();

                if (nodeName.equals("add")) {
                    Node valueNode = child.getAttributes().getNamedItem("value");
                    if (valueNode != null)
                        result.add(valueNode.getNodeValue());
                } else if (nodeName.equals("clear")) {
                    result.clear();
                }

                child = child.getNextSibling();
            }
        }

        return result;
    }

    File getAspxRoot(File rootDirectory) {
        Collection aspxCsFiles = FileUtils.listFiles(rootDirectory,
                new FileExtensionFileFilter(".config"), TrueFileFilter.INSTANCE);

        int shortestPathLength = Integer.MAX_VALUE;
        File returnFile = rootDirectory;

        for (Object aspxCsFile : aspxCsFiles) {
            if (aspxCsFile instanceof File) {
                File file = (File) aspxCsFile;
                if (file.isFile() && (file.getName().equalsIgnoreCase("web.config"))) {
                    if (file.getAbsolutePath().length() < shortestPathLength) {
                        shortestPathLength = file.getAbsolutePath().length();
                        returnFile = file.getParentFile();
                    }
                }
            }
        }

        // reference comparison ok here because we're checking to see whether the reference has changed
        assert returnFile != rootDirectory : "web.config not found.";
        return returnFile;
    }

    void collapseToEndpoints(Collection<AspxCsParser> csParsers,
                             Collection<AspxParser> aspxParsers,
                             File solutionDirectory,
                             File projectDirectory,
                             List<String> defaultPages) {
        Map<String, AspxParser> aspxParserMap = map();
        Map<String, AspxCsParser> aspxCsParserMap = map();

        File aspxRootDirectory = getAspxRoot(projectDirectory);

        for (AspxCsParser csParser : csParsers) {
            aspxCsParserMap.put(csParser.aspName, csParser);
        }

        for (AspxParser aspxParser : aspxParsers) {
            aspxParserMap.put(aspxParser.aspName, aspxParser);
        }

        for (Map.Entry<String, AspxParser> entry : aspxParserMap.entrySet()) {
            String key = entry.getKey() + ".cs", key2 = entry.getKey().replaceFirst("\\.aspx", ".cs");
            AspxCsParser aspxCsParser = null;
            if (aspxCsParserMap.containsKey(key)) {
                aspxCsParser = aspxCsParserMap.get(key);
            } else if (aspxCsParserMap.containsKey(key2)) {
                aspxCsParser = aspxCsParserMap.get(key2);
            }

            if (aspxCsParser == null) {
                continue;
            }

            WebFormsEndpointExplicit primaryEndpoint = new WebFormsEndpointExplicit(solutionDirectory, projectDirectory, aspxRootDirectory, entry.getValue(), aspxCsParser);
            endpoints.add(primaryEndpoint);

            primaryEndpoint.addVariant(new WebFormsEndpointExtensionless(solutionDirectory, projectDirectory, aspxRootDirectory, entry.getValue(), aspxCsParser));

            for (String defaultPageName : defaultPages) {
                if (defaultPageName.equalsIgnoreCase(entry.getKey())) {
                    primaryEndpoint.addVariant(new WebFormsEndpointImplicit(solutionDirectory, projectDirectory, aspxRootDirectory, entry.getValue(), aspxCsParser));
                    break;
                }
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
}
