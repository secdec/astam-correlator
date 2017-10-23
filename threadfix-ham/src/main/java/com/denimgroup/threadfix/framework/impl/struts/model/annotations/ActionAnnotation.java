package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

//  See: https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Actionannotation

import java.util.HashMap;
import java.util.Map;

public class ActionAnnotation extends Annotation {
    String boundUrl;
    Map<String, String> params = new HashMap<String, String>();

    public void setBoundUrl(String url) {
        boundUrl = url;
    }

    public String getBoundUrl() {
        return boundUrl;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }
}
