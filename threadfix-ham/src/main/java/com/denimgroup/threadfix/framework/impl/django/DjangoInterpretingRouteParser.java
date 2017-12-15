package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;

import java.io.File;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class DjangoInterpretingRouteParser {

    PythonCodeCollection codebase;

    public DjangoInterpretingRouteParser(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    public Map<String, DjangoRoute> executeRoutingFile(String filePath) {
        PythonModule targetModule = codebase.findByFilePath(filePath);
        if (targetModule.findChild("url_patterns") == null) {
            return map();
        }

        PythonInterpreter interpreter = new PythonInterpreter(this.codebase);

        interpreter.run(new File(filePath), 0, Integer.MAX_VALUE, targetModule);

        ExecutionContext executionContext = interpreter.getExecutionContext();
        PythonValue urlPatternsValue = executionContext.getWorkingMemory().get(targetModule.findChild("url_patterns").getFullName());

        if (urlPatternsValue == null || !(urlPatternsValue instanceof PythonArray)) {
            return map();
        }

        PythonArray urlPatterns = (PythonArray)urlPatternsValue;

        Map<String, DjangoRoute> result = map();
        for (PythonValue entry : urlPatterns.getEntries()) {
            if (!(entry instanceof PythonObject)) {
                continue;
            }

            PythonObject object = (PythonObject)entry;
            PythonValue urlValue = object.getMemberVariable("url");
            PythonValue viewValue = object.getMemberVariable("view");

            if (urlValue == null || viewValue == null || !(urlValue instanceof PythonStringPrimitive)) {
                continue;
            }
        }
        return result;
    }

}
