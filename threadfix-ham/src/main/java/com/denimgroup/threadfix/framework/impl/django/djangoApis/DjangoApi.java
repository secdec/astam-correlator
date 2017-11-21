package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;

public interface DjangoApi {

    String getIdentifier();
    void apply(PythonCodeCollection codebase);
    void applyPostLink(PythonCodeCollection codebase);

}
