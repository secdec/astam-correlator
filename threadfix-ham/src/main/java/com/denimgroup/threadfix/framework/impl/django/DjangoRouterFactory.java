package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.routers.DefaultRouter;
import com.denimgroup.threadfix.framework.impl.django.routers.DjangoRouter;

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
