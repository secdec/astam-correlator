package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.*;

import java.util.Map;

public class DjangoInternationalizationDetector implements PythonVisitor {

    private boolean i18RefFound = false;

    public boolean isLocalized() {
        return i18RefFound;
    }

    private void detectInScope(AbstractPythonScope scope) {
        if (i18RefFound) {
            return;
        }
        for (Map.Entry<String, String> entry : scope.getImports().entrySet()) {
            if (entry.getValue().equals("django.utils.translation")) {
                i18RefFound = true;
            }
        }
    }

    @Override
    public void visitModule(PythonModule pyModule) {
        detectInScope(pyModule);
    }

    @Override
    public void visitClass(PythonClass pyClass) {
        detectInScope(pyClass);
    }

    @Override
    public void visitFunction(PythonFunction pyFunction) {
        detectInScope(pyFunction);
    }

    @Override
    public void visitPublicVariable(PythonPublicVariable pyVariable) {
        detectInScope(pyVariable);
    }
}
