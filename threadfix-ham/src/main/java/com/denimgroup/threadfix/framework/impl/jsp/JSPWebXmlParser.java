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
                    servletClass = null;

            Node servletNode = servletNodes.item(i);
            Node childNode = servletNode.getFirstChild();
            while (childNode != null) {

                String nodeName = childNode.getNodeName();

                if (nodeName.equals("servlet-name")) {
                    servletName = childNode.getTextContent();
                } else if (nodeName.equals("servlet-class")) {
                    servletClass = childNode.getTextContent();
                }

                childNode = childNode.getNextSibling();
            }

            JSPWebXmlServlet newServlet = new JSPWebXmlServlet(servletName, servletClass);
            config.addServlet(newServlet);
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
        for (JSPWebXmlServletMapping mapping : config.getServletMappings()) {
            JSPWebXmlServlet servlet = config.findServletByName(mapping.getServletName());
            if (servlet != null) {
                mapping.setMappedServlet(servlet);
            } else {
                LOG.info("Couldn't find servlet with the name ".concat(mapping.getServletName()));
            }
        }
    }

    public JSPWebXmlConfiguration getConfiguration() {
        return configuration;
    }
}
