package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.routers.DefaultRouter;

public class DjangoRouterFactory {

    PythonCodeCollection codebase;

    public DjangoRouterFactory(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    public DjangoRouter makeRouterFor(String identifier) {
        if (identifier.equalsIgnoreCase("DefaultRouter")) {
            return new DefaultRouter(codebase);
        }
        return null;
    }

}
