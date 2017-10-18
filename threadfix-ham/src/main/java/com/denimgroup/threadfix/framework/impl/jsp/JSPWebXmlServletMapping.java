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
