package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

//  See: https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Actionannotation

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ActionAnnotation extends Annotation {
    String boundUrl;
    List<ResultAnnotation> results = list();
    Map<String, String> params = new HashMap<String, String>();
    String explicitClassName;



    public String getBoundUrl() {
        return boundUrl;
    }

    public Collection<ResultAnnotation> getResults() {
        return results;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getExplicitClassName() {
        return explicitClassName;
    }



    public void setBoundUrl(String url) {
        boundUrl = url;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }

    public void addResult(ResultAnnotation result) {
        results.add(result);
    }

    public void setExplicitClassName(String className) {
        explicitClassName = className;
    }
}
