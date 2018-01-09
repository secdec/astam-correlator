package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonVisitor;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.map;

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

    public static void printDuplicateStatements(PythonCodeCollection code) {
        final Map<String, Integer> visitedSymbols = map();
        code.traverse(new AbstractPythonVisitor() {
            @Override
            public void visitAny(AbstractPythonStatement statement) {
                String fullName = statement.getFullName();
                if (visitedSymbols.containsKey(fullName)) {
                    int numVisits = visitedSymbols.get(fullName);
                    visitedSymbols.put(fullName, ++numVisits);
                } else {
                    visitedSymbols.put(fullName, 1);
                }
                super.visitAny(statement);
            }
        });

        int numDuplicates = 0;

        for (Map.Entry<String, Integer> entry : visitedSymbols.entrySet()) {
            if (entry.getValue() > 1) {
                ++numDuplicates;
                LOG.info(entry.getKey() + " had " + entry.getValue() + " duplicates");
            }
        }

        LOG.info(numDuplicates + " total duplicates");
    }

}
