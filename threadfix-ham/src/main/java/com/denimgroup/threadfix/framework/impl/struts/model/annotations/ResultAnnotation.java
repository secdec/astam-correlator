package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  See:    https://struts.apache.org/docs/result-annotation.html
//          https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Resultannotation


public class ResultAnnotation extends Annotation {

    String resultName = "success";
    String location; // named "location" or "value" as param name
    String type; // "NullResult", "PlainTextResult", etc.

    Map<String, String> params = new HashMap<String, String>();

    public void setResultName(String name) {
        resultName = name;
    }

    public void setResultLocation(String location) {
        this.location = location;
    }

    public void setResultType(String type) {
        this.type = type;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }

    public String getResultName() {
        return resultName;
    }

    public String getResultLocation() {
        return location;
    }

    public String getResultType() {
        return type;
    }

    public Map<String, String> getParameters() {
        return params;
    }


    @Override
    public boolean equals(Object obj) {
        if (!ResultAnnotation.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        ResultAnnotation other = (ResultAnnotation)obj;
        if ((this.resultName == null) != (other.resultName == null)) {
            return false;
        }

        if (this.codeLine != other.codeLine) {
            return false;
        }

        if (this.params.size() != other.params.size()) {
            return false;
        }

        for (Map.Entry<String, String> param : params.entrySet()) {
            if (!other.params.containsKey(param.getKey())) {
                return false;
            }

            String otherVal = other.params.get(param.getKey());
            if (!otherVal.equals(param.getValue())) {
                return false;
            }
        }

        return true;
    }
}
