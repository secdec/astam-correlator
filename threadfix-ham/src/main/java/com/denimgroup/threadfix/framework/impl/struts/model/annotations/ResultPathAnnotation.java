package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

//  See: https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-ResultPathannotation

public class ResultPathAnnotation extends Annotation {

    String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String fileEndpoint) {
        location = fileEndpoint;
    }

}
