package com.denimgroup.threadfix.framework.impl.jsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSPServlet {
    private String packageName;
    private String className;
    private String filePath;

    private Map<Integer, List<String>> parameters = new HashMap<Integer, List<String>>();

    public JSPServlet(String packageName, String className, String filePath) {
        this.packageName = packageName;
        this.className = className;
        this.filePath = filePath;
    }

    public JSPServlet(String packageName, String className, String filePath, Map<Integer, List<String>> parameters) {
        this(packageName, className, filePath);
        if (parameters != null) {
            this.parameters = parameters;
        }
    }


    public void addParameter(int lineNumber, String parameterName) {
        if (!parameters.containsKey(lineNumber)) {
            parameters.put(lineNumber, new ArrayList<String>());
        }

        List<String> knownParameters = parameters.get(lineNumber);
        knownParameters.add(parameterName);
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
