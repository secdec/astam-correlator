package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;

public interface DjangoApi {

    void apply(PythonCodeCollection codebase);

}
