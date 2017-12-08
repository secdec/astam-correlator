package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCachingExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.FileReadUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PythonInterpreter {

    ExecutionContext executionContext;
    PythonExpressionParser expressionParser;

    public PythonInterpreter(PythonCodeCollection codebase) {
        executionContext = new ExecutionContext(codebase);
        expressionParser = new PythonCachingExpressionParser();
    }

    public PythonInterpreter(@Nonnull ExecutionContext executionContext) {
        this.executionContext = executionContext;
        expressionParser = new PythonCachingExpressionParser();
    }

    public PythonInterpreter(@Nonnull PythonCodeCollection codebase, PythonValue valueContext) {
        this.executionContext = new ExecutionContext(codebase, valueContext);
        expressionParser = new PythonCachingExpressionParser();
    }


    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public PythonValue run(@Nonnull String code) {
        return run(code, null);
    }

    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope) {
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
        return run(expression, scope, null);
    }

    public PythonValue run(@Nonnull PythonExpression expression, AbstractPythonStatement scope, PythonValue selfValue) {
        //  Real run function

        boolean usesNewContext =
                selfValue != this.executionContext.selfValue ||
                scope != this.executionContext.scope;

        ExpressionInterpreter interpreter = expression.makeInterpreter();
        if (interpreter == null) {
            return new PythonIndeterminateValue();
        } else {
            if (usesNewContext) {
                pushExecutionContext(scope, selfValue);
            }

            List<PythonValue> dependencies = expression.getSubValues();

            for (PythonValue subValue : dependencies) {
                if (subValue instanceof PythonExpression) {
                    PythonValue resolvedValue = run((PythonExpression) subValue, scope);
                    expression.resolveSubValue(subValue, resolvedValue);
                }
            }

            PythonValue result = interpreter.interpret(this, expression);

            if (usesNewContext) {
                popExecutionContext();
            }

            if (result != null) {
                return result;
            } else {
                return new PythonIndeterminateValue();
            }
        }
    }



    private void pushExecutionContext(AbstractPythonStatement scope, PythonValue selfValue) {
        ExecutionContext newContext = new ExecutionContext(this.executionContext.codebase, selfValue, scope);
        newContext.parentContext = this.executionContext;
        this.executionContext = newContext;
    }

    private void popExecutionContext() {
        ExecutionContext parentContext = this.executionContext.parentContext;
        if (parentContext != null) {
            this.executionContext = parentContext;
        }
    }

}
