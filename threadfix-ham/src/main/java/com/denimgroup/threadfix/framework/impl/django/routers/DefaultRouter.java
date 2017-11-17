package com.denimgroup.threadfix.framework.impl.django.routers;

import com.denimgroup.threadfix.framework.impl.django.DjangoRoute;
import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonScope;
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
    public void parseConstructorParameters(List<String> parameters) {
        for (String param: parameters) {
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
    public void parseMethod(String name, List<String> parameters) {
        if (name.equalsIgnoreCase("register")) {
            String path = parameters.get(0);
            if (path.startsWith("r")) {
                path = path.substring(1);
            }

            if (path.startsWith("'") || path.startsWith("\"")) {
                path = path.substring(1);
            }

            if (path.endsWith("'") || path.endsWith("\"")) {
                path = path.substring(0, path.length() - 1);
            }

            String controller = parameters.get(1);
            String viewBaseName = null; // Indicates the text format of related view files, unused
            if (parameters.size() > 2) {
                for (int i = 2; i < parameters.size(); i++) {
                    String param = parameters.get(i);
                    if (param.startsWith("base_name")) {
                        viewBaseName = param.split("\\=")[1];
                    }
                }
            }

            AbstractPythonScope controllerObj = this.codebase.findByFullName(controller);
            DjangoRoute route;
            if (controllerObj != null) {
                route = new DjangoRoute(path, controllerObj.getSourceCodePath());
            } else {
                route = new DjangoRoute(path, controller);
            }

            routes.add(route);
        }
    }

    @Override
    public Collection<DjangoRoute> getRoutes() {
        return routes;
    }


}
