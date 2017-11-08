package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DeviseModule {
    private String name;
    private List<PathHttpMethod> endpoints = list();
    private String controllerName;
    private String controllerModule;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PathHttpMethod> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<PathHttpMethod> endpoints) {
        this.endpoints = endpoints;
    }

    public void addEndpoint(PathHttpMethod endpoint) {
        this.endpoints.add(endpoint);
    }
}
