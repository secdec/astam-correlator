package com.denimgroup.threadfix.framework.impl.django.routers;

import com.denimgroup.threadfix.framework.impl.django.DjangoRoute;
import com.denimgroup.threadfix.framework.impl.django.DjangoRouter;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DefaultRouter implements DjangoRouter {

    PythonCodeCollection codebase;
    boolean trailingSlash = true;
    List<DjangoRoute> routes = list();

    public DefaultRouter(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    @Override
    public String getUrlsName() {
        return "urls";
    }

    @Override
    public void parseConstructorParameters(String parameters) {
        String[] paramParts = parameters.split(",");
        for (String param: paramParts) {
            String[] paramValue = param.split("=");
            if (paramValue.length == 2) {
                String name = paramValue[0];
                String value = paramValue[1];

                if (name.equalsIgnoreCase("trailing_slash")) {
                    trailingSlash = value.equalsIgnoreCase("True");
                }
            }
        }
    }

    @Override
    public void parseMethod(String name, String parameters) {
        if (name.equalsIgnoreCase("register")) {
            String[] params = parameters.split(",");

            String path = params[0];
            if (path.startsWith("r")) path = path.substring(1);
            if (path.startsWith("'")) path = path.substring(1);
            if (path.endsWith  ("'")) path = path.substring(0, path.length() - 1);

            String view = params[1];

            DjangoRoute route = new DjangoRoute(path, view);
            routes.add(route);
        }
    }

    @Override
    public Collection<DjangoRoute> getRoutes() {
        return routes;
    }


}
