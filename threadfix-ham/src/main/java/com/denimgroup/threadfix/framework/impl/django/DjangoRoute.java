package com.denimgroup.threadfix.framework.impl.django;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 6/13/2017.
 */
public class DjangoRoute {
    private String url;
    private String viewPath;
    //function name, http method
    private Map<String, String> httpMethods = map();

    public DjangoRoute(String url, String viewPath, String function, String httpMethod) {
        this.setUrl(url);
        this.setViewPath(viewPath);
        this.addHttpMethod(function, httpMethod);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getViewPath() {
        return viewPath;
    }

    private void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public Map<String, String> getHttpMethods() {
        return httpMethods;
    }

    public void addHttpMethod(String function, String httpMethod) {
        this.httpMethods.put(function, httpMethod);
    }
}
