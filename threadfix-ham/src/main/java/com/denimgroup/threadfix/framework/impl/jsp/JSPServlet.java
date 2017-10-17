package com.denimgroup.threadfix.framework.impl.jsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSPServlet {
    private String packageName;
    private String className;
    private String filePath;

    //  TODO - Parameter parsing
    private Map<Integer, List<String>> parameters = new HashMap<Integer, List<String>>();

    public JSPServlet(String packageName, String className, String filePath) {
        this.packageName = packageName;
        this.className = className;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getAbsoluteName() {
        return packageName + "." + className;
    }

    public Map<Integer, List<String>> getParameters() {
        return parameters;
    }
}
