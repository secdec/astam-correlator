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

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

enum JSPServletMappingType {
    UNKNOWN,
    MAP_CLASS_SERVLET,
    MAP_JSP_SERVLET
}

public class JSPWebXmlServletMapping {

    private String servletName;
    private List<String> urlPatterns;
    private Object mappedServlet;

    JSPServletMappingType mappingType = JSPServletMappingType.UNKNOWN;

    public JSPWebXmlServletMapping(String servletName, List<String> urlPatterns) {
        this.servletName = servletName;
        this.urlPatterns = urlPatterns;
    }

    public void setMappedServlet(JSPWebXmlServlet servlet) {
        mappedServlet = servlet;
        mappingType = JSPServletMappingType.MAP_CLASS_SERVLET;
    }

    public void setMappedServlet(JSPWebXmlJspServlet jspServlet) {
        mappedServlet = jspServlet;
        mappingType = JSPServletMappingType.MAP_JSP_SERVLET;
    }

    public JSPWebXmlServlet getMappedClassServlet() {
        if (mappingType != JSPServletMappingType.MAP_CLASS_SERVLET || mappedServlet == null) {
            return null;
        }

        return (JSPWebXmlServlet)mappedServlet;
    }

    public JSPWebXmlJspServlet getMappedJspServlet() {
        if (mappingType != JSPServletMappingType.MAP_JSP_SERVLET || mappedServlet == null) {
            return null;
        }

        return (JSPWebXmlJspServlet)mappedServlet;
    }

    public JSPServletMappingType getMappingType() {
        return mappingType;
    }

    public String getServletName() {
        return servletName;
    }

    public List<String> getUrlPatterns() {
        List<String> result = list();
        result.addAll(urlPatterns);
        return result;
    }

    //  TODO - Servlets can have multiple URL bindings, when mapping a finding need to iterate through bindings and find best match
    public boolean matchesUrlPattern(String url) {
        return false;
    }
}
