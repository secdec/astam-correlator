package com.denimgroup.threadfix.framework.impl.django.routers;

import com.denimgroup.threadfix.framework.impl.django.DjangoRoute;

import java.util.Collection;
import java.util.List;

public interface DjangoRouter {

    String getUrlsName();

    void parseConstructorParameters(List<String> parameters);
    void parseMethod(String name, List<String> parameters);

    Collection<DjangoRoute> getRoutes();

}
