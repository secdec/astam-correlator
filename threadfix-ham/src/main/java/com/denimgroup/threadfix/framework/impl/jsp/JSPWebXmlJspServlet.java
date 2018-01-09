package com.denimgroup.threadfix.framework.impl.jsp;

//  An XML servlet declaration mapping a JSP file
public class JSPWebXmlJspServlet {
    private String filePath;
    private String servletName;

    public JSPWebXmlJspServlet(String name, String filePath) {
        this.filePath = filePath;
        this.servletName = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getServletName() {
        return servletName;
    }
}
