package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.*;

import java.util.Map;

public class DjangoInternationalizationDetector extends AbstractPythonVisitor {

    private boolean i18RefFound = false;

    public boolean isLocalized() {
        return i18RefFound;
    }

    private void detectInScope(AbstractPythonStatement scope) {
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
    public void visitAny(AbstractPythonStatement statement) {
        detectInScope(statement);
    }
}
