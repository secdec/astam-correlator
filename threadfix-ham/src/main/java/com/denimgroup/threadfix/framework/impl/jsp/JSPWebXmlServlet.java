package com.denimgroup.threadfix.framework.impl.jsp;

public class JSPWebXmlServlet {
    private String servletName;
    private String servletClass;

    public JSPWebXmlServlet(String servletName, String servletClass) {
        this.servletName = servletName;
        this.servletClass = servletClass;
    }

    public String getServletClass() {
        return servletClass;
    }

    public String getServletName() {
        return servletName;
    }
}
