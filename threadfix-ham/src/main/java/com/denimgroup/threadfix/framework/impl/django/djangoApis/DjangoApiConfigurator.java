package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.DjangoProject;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoApiConfigurator {

    //  Adding support for more standard Django APIs and third-party APIs can be inserted here
    private List<DjangoApi> apis = list(
            (DjangoApi)new DjangoAdminApi(),
            (DjangoApi)new DjangoConfUrlsApi(),
            (DjangoApi)new DjangoUrlsApi()
    );

    public DjangoApiConfigurator(DjangoProject project) {
        for (DjangoApi api : apis) {
            api.configure(project);
        }
    }

    /**
     * Attaches known django API modules, objects, and functions to the given codebase.
     */
    public void applySchema(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchema(codebase);
        }
    }

    public void applySchemaPostLink(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchemaPostLink(codebase);
        }
    }

    public void applyRuntime(PythonInterpreter runtime) {
        for (DjangoApi api : apis) {
            api.applyRuntime(runtime);
        }
    }

}
