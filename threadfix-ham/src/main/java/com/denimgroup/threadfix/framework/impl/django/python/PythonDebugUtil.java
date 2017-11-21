package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;
import java.util.Map;

public class PythonDebugUtil {

    private static SanitizedLogger LOG = new SanitizedLogger(PythonDebugUtil.class);

    public static void printFullTypeNames(PythonCodeCollection code) {
        Collection<AbstractPythonStatement> scopes = code.getAll();
        for (AbstractPythonStatement scope : scopes) {
            String output = "type: " + scope.getFullName() + " -> " + scope.getSourceCodePath();
            //LOG.debug(output);
            LOG.info(output);
        }
    }

    public static void printFullImports(PythonCodeCollection code) {
        Collection<AbstractPythonStatement> scopes = code.getAll();
        for (AbstractPythonStatement scope : scopes) {
            for (Map.Entry<String, String> entry : scope.getImports().entrySet()) {
                String output ="import: " + entry.getValue() + " -> " + entry.getKey() + " (" + scope.getSourceCodePath() + ")";
                //LOG.debug(output);
                LOG.info(output);
            }
        }
    }

}
