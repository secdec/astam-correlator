package com.denimgroup.threadfix.framework.impl.struts.model;

import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsDetectedParameter {

    String queryMethod;
    public String paramName;
    public String paramType;
    public String targetEndpoint;
    public String sourceFile;
    public List<String> allowedValues = list();
}
