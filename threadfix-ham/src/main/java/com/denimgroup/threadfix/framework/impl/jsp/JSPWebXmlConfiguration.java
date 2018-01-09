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
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPWebXmlConfiguration {

    private static final SanitizedLogger LOG = new SanitizedLogger("JSPWebXmlConfiguration");

    public final List<String> DefaultWelcomeFiles = list("index.jsp");

    private List<String> welcomeFileList = list();
    private List<JSPWebXmlServlet> servlets = list();
    private List<JSPWebXmlJspServlet> jspServlets = list();
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

    public void addServlet(JSPWebXmlJspServlet jspServlet) {
        jspServlets.add(jspServlet);
    }

    public void addServletMapping(JSPWebXmlServletMapping servletMapping) {
        servletMappings.add(servletMapping);
    }

    public List<String> getWelcomeFileList() {
        return welcomeFileList;
    }

    public List<JSPWebXmlServlet> getClassServlets() {
        return servlets;
    }

    public List<JSPWebXmlJspServlet> getJspServlets() { return jspServlets; }

    public List<JSPWebXmlServletMapping> getAllServletMappings() {
        return servletMappings;
    }

    public List<JSPWebXmlServletMapping> getServletMappings(JSPServletMappingType mappingType) {
        List<JSPWebXmlServletMapping> classServletMappings = list();

        for (JSPWebXmlServletMapping mapping : servletMappings) {
            if (mapping.getMappingType() == mappingType) {
                classServletMappings.add(mapping);
            }
        }

        return classServletMappings;
    }




    public JSPWebXmlServlet findClassServletByName(String name) {
        for (JSPWebXmlServlet servlet : servlets) {
            if (servlet.getServletName().equalsIgnoreCase(name)) {
                return servlet;
            }
        }

        return null;
    }

    public JSPWebXmlJspServlet findJspServletByName(String name) {
        for (JSPWebXmlJspServlet jspServlet : jspServlets) {
            if (jspServlet.getServletName().equalsIgnoreCase(name)) {
                return jspServlet;
            }
        }

        return null;
    }
}
