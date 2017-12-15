package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoApiConfigurator {

    //  Adding support for more standard Django APIs and third-party APIs can be inserted here
    private final static List<DjangoApi> apis = list(
            (DjangoApi)new DjangoAdminApi(),
            (DjangoApi)new DjangoConfUrlsApi(),
            (DjangoApi)new DjangoUrlsApi()
    );

    /**
     * Attaches known django API modules, objects, and functions to the given codebase.
     */
    public static void applySchema(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchema(codebase);
        }
    }

    public static void applySchemaPostLink(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchemaPostLink(codebase);
        }
    }

    public static void applyRuntime(PythonInterpreter runtime) {
        for (DjangoApi api : apis) {
            api.applyRuntime(runtime);
        }
    }

}
