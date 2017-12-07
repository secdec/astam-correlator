package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.FileReadUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public class PythonInterpreter {

    ExecutionContext executionContext;

    public PythonInterpreter(PythonCodeCollection codebase) {
        executionContext = new ExecutionContext(codebase);
    }

    public PythonInterpreter(@Nonnull ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public PythonInterpreter(@Nonnull PythonCodeCollection codebase, PythonValue valueContext) {
        this.executionContext = new ExecutionContext(codebase, valueContext);
    }


    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public PythonValue run(@Nonnull String code) {
        return run(code, null);
    }

    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope) {
        PythonExpressionParser expressionParser = new PythonExpressionParser();
        return run(expressionParser.processString(code), scope);
    }

    public PythonValue run(@Nonnull PythonExpression expression) {
        return run(expression, null);
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine) {
        return run(targetFile, startLine, endLine, null);
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine, AbstractPythonStatement scope) {
        List<String> lines = FileReadUtils.readLinesCondensed(targetFile.getAbsolutePath(), startLine, endLine);
        PythonValue lastValue = null;
        for (String line : lines) {
            lastValue = run(line, scope);
        }
        return lastValue;
    }




    public PythonValue run(@Nonnull PythonExpression expression, AbstractPythonStatement scope) {
        //  Real run function

        ExpressionInterpreter interpreter = expression.makeInterpreter();
        if (interpreter == null) {
            return new PythonIndeterminateValue();
        } else {
            return interpreter.interpret(expression, executionContext);
        }

    }

}
