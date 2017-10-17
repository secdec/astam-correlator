package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPWebXmlConfiguration {

    private static final SanitizedLogger LOG = new SanitizedLogger("JSPWebXmlConfiguration");

    public final List<String> DefaultWelcomeFiles = list("index.jsp");

    private List<String> welcomeFileList = list();
    private List<JSPWebXmlServlet> servlets = list();
    private List<JSPWebXmlServletMapping> servletMappings = list();

    public JSPWebXmlConfiguration() {
        welcomeFileList.addAll(DefaultWelcomeFiles);
    }

    public void addWelcomeFile(String fileName) {
        for (String existingFile : welcomeFileList) {
            if (existingFile.equals(fileName)) {
                LOG.debug("Welcome file " + fileName + " is already registered, skipping");
                return;
            }
        }

        welcomeFileList.add(fileName);
    }

    public void addServlet(JSPWebXmlServlet servlet) {
        servlets.add(servlet);
    }

    public void addServletMapping(JSPWebXmlServletMapping servletMapping) {
        servletMappings.add(servletMapping);
    }

    public List<String> getWelcomeFileList() {
        return welcomeFileList;
    }

    public List<JSPWebXmlServlet> getServlets() {
        return servlets;
    }

    public List<JSPWebXmlServletMapping> getServletMappings() {
        return servletMappings;
    }



    public JSPWebXmlServlet findServletByName(String name) {
        for (JSPWebXmlServlet servlet : servlets) {
            if (servlet.getServletName().equalsIgnoreCase(name)) {
                return servlet;
            }
        }

        return null;
    }
}
