package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoApiConfigurator {

    //  Adding support for more standard Django APIs and third-party APIs can be inserted here
    final static List<DjangoApi> apis = list(
            (DjangoApi)new DjangoAdminApi()
    );

    /**
     * Attaches known django API modules, objects, and functions to the given codebase.
     */
    public static void apply(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.apply(codebase);
        }
    }

    public static void applyPostLink(PythonCodeCollection codebase) {

    }

}
