package com.denimgroup.threadfix.framework.impl.django;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 6/13/2017.
 */
public class DjangoRoute {
    private String url;
    private String viewPath;
    private List<String> httpMethods = list();
    private List<String> parameters = list();

    public DjangoRoute(String url, String viewPath) {
        this.url = url;
        this.viewPath = viewPath;
    }

    public String getUrl() {
        return url;
    }

    public String getViewPath() {
        return viewPath;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void addHttpMethod(String httpMethod) {
        httpMethods.add(httpMethod);
    }

    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
}
