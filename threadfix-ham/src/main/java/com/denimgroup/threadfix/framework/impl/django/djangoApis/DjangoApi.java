package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.DjangoProject;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;

public interface DjangoApi {

    void configure(DjangoProject project);

    String getIdentifier();
    void applySchema(PythonCodeCollection codebase);
    void applySchemaPostLink(PythonCodeCollection codebase);
    void applyRuntime(PythonInterpreter runtime);
}
