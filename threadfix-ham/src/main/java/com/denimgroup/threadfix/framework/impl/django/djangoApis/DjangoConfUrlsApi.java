package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls.IncludeFunction;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls.UrlFunction;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;

public class DjangoConfUrlsApi extends AbstractDjangoApi {
    @Override
    public String getIdentifier() {
        return "django.conf.urls";
    }

    @Override
    public void applySchema(PythonCodeCollection codebase) {

        PythonModule baseModule = makeModulesFromFullName(getIdentifier());

        baseModule.addChildStatement(new UrlFunction());
        baseModule.addChildStatement(new IncludeFunction());

        tryAddScopes(codebase, baseModule);

    }

    @Override
    public void applySchemaPostLink(PythonCodeCollection codebase) {

    }

    @Override
    public void applyRuntime(PythonInterpreter runtime) {

    }
}
