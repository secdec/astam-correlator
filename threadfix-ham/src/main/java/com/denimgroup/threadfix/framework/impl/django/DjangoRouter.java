package com.denimgroup.threadfix.framework.impl.django;

import java.util.Collection;

public interface DjangoRouter {

    String getUrlsName();

    void parseConstructorParameters(String parameters);
    void parseMethod(String name, String parameters);

    Collection<DjangoRoute> getRoutes();

}
