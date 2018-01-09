package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls.PathFunction;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls.RePathFunction;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;

public class DjangoUrlsApi extends AbstractDjangoApi {
    @Override
    public String getIdentifier() {
        return "django.urls";
    }

    @Override
    public void applySchema(PythonCodeCollection codebase) {
        PythonModule baseModule = makeModulesFromFullName(getIdentifier());

        baseModule.addChildStatement(new PathFunction());
        baseModule.addChildStatement(new RePathFunction());

        tryAddScopes(codebase, baseModule);
    }

    @Override
    public void applySchemaPostLink(PythonCodeCollection codebase) {

    }

    @Override
    public void applyRuntime(PythonInterpreter runtime) {

    }
}
