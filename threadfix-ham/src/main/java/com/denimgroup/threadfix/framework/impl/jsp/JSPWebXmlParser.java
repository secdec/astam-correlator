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
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.sun.org.apache.xpath.internal.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPWebXmlParser {

    private static final SanitizedLogger LOG = new SanitizedLogger("JSPWebXmlParser");

    JSPWebXmlConfiguration configuration;

    public JSPWebXmlParser(File webXmlFile) {

        JSPWebXmlConfiguration config = new JSPWebXmlConfiguration();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }

        try {
            doc = builder.parse(webXmlFile);
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        XPathExpression welcomeListQuery;
        XPathExpression servletsQuery;
        XPathExpression servletMappingsQuery;

        try {
            welcomeListQuery = xPath.compile("/web-app/welcome-file-list/welcome-file");
            servletsQuery = xPath.compile("/web-app/servlet");
            servletMappingsQuery = xPath.compile("/web-app/servlet-mapping");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return;
        }

        NodeList welcomeListNodes;
        NodeList servletNodes;
        NodeList servletMappingNodes;
        try {
            welcomeListNodes = (NodeList)welcomeListQuery.evaluate(doc, XPathConstants.NODESET);
            servletNodes = (NodeList)servletsQuery.evaluate(doc, XPathConstants.NODESET);
            servletMappingNodes = (NodeList)servletMappingsQuery.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return;
        }

        populateWelcomeFiles(config, welcomeListNodes);
        populateServlets(config, servletNodes);
        populateServletMappings(config, servletMappingNodes);

        bindServletMappings(config);

        this.configuration = config;
    }

    private void populateWelcomeFiles(JSPWebXmlConfiguration config, NodeList welcomeFileNodes) {
        for (int i = 0; i < welcomeFileNodes.getLength(); i++) {
            Node welcomeNode = welcomeFileNodes.item(i);
            String fileName = welcomeNode.getTextContent();
            config.addWelcomeFile(fileName);
        }
    }

    private void populateServlets(JSPWebXmlConfiguration config, NodeList servletNodes) {
        for (int i = 0; i < servletNodes.getLength(); i++) {
            String servletName = null,
                    servletClass = null,
                    servletJsp = null;

            JSPServletMappingType servletType = JSPServletMappingType.UNKNOWN;

            Node servletNode = servletNodes.item(i);
            Node childNode = servletNode.getFirstChild();
            while (childNode != null) {

                String nodeName = childNode.getNodeName();

                if (nodeName.equalsIgnoreCase("servlet-name")) {
                    servletName = childNode.getTextContent();
                } else if (nodeName.equalsIgnoreCase("servlet-class")) {
                    servletClass = childNode.getTextContent();
                    servletType = JSPServletMappingType.MAP_CLASS_SERVLET;
                } else if (nodeName.equalsIgnoreCase("jsp-file")) {
                    servletJsp = childNode.getTextContent();
                    servletType = JSPServletMappingType.MAP_JSP_SERVLET;
                }

                childNode = childNode.getNextSibling();
            }

            switch (servletType) {
                case MAP_CLASS_SERVLET:
                    JSPWebXmlServlet newServlet = new JSPWebXmlServlet(servletName, servletClass);
                    config.addServlet(newServlet);
                    break;

                case MAP_JSP_SERVLET:
                    JSPWebXmlJspServlet newJspServlet = new JSPWebXmlJspServlet(servletName, servletJsp);
                    config.addServlet(newJspServlet);
                    break;
            }

        }
    }

    private void populateServletMappings(JSPWebXmlConfiguration config, NodeList servletMappingNodes) {
        for (int i = 0; i < servletMappingNodes.getLength(); i++) {
            String mappedServletName = null;
            List<String> urlPatterns = list();

            Node mappingNode = servletMappingNodes.item(i);
            Node childNode = mappingNode.getFirstChild();
            while (childNode != null) {

                String nodeName = childNode.getNodeName();

                if (nodeName.equals("servlet-name")) {
                    mappedServletName = childNode.getTextContent();
                } else if (nodeName.equals("url-pattern")) {
                    urlPatterns.add(childNode.getTextContent());
                }

                childNode = childNode.getNextSibling();
            }

            JSPWebXmlServletMapping newMapping = new JSPWebXmlServletMapping(mappedServletName, urlPatterns);
            config.addServletMapping(newMapping);
        }
    }

    private void bindServletMappings(JSPWebXmlConfiguration config) {

        for (JSPWebXmlServletMapping mapping : config.getAllServletMappings()) {
            JSPWebXmlServlet servlet = config.findClassServletByName(mapping.getServletName());
            JSPWebXmlJspServlet jspServlet = config.findJspServletByName(mapping.getServletName());

            if (servlet != null) {
                mapping.setMappedServlet(servlet);
            } else if (jspServlet != null) {
                mapping.setMappedServlet(jspServlet);
            } else {
                LOG.info("Couldn't find servlet with the name " + mapping.getServletName());
            }
        }
    }

    public JSPWebXmlConfiguration getConfiguration() {
        return configuration;
    }
}
