package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCachingExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.InterpreterUtil;
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
    PythonValueBuilder valueBuilder = new PythonValueBuilder();

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
        code = Language.stripComments(code);

        PythonExpression expression = expressionParser.processString(code);
        if (expression instanceof IndeterminateExpression) {
            return valueBuilder.buildFromSymbol(code);
        } else {
            PythonValue evaluated = run(expression, scope);
            if (!(evaluated instanceof PythonIndeterminateValue)) {
                return evaluated;
            } else {
                return valueBuilder.buildFromSymbol(code);
            }
        }
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

            resolveDependencies(expression, scope);

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

    private void resolveDependencies(PythonExpression expression, AbstractPythonStatement scope) {
        List<PythonValue> dependencies = expression.getSubValues();

        for (PythonValue subValue : dependencies) {
            if (subValue instanceof PythonExpression) {
                PythonValue resolvedValue = run((PythonExpression) subValue, scope);
                expression.resolveSubValue(subValue, resolvedValue);
            } else if (subValue instanceof PythonVariable) {
                PythonVariable asVariable = (PythonVariable)subValue;

                if (asVariable.getValue() == null && asVariable.getLocalName() != null) {
                    asVariable.setValue(executionContext.resolveSymbol(asVariable.getLocalName()));
                }

                PythonValue resolvedValue = asVariable.getValue();

                while (resolvedValue != null && resolvedValue instanceof PythonVariable) {
                    PythonVariable valueAsVariable = (PythonVariable)resolvedValue;
                    if (valueAsVariable.getValue() != null) {
                        break;
                    }

                    if (valueAsVariable.getLocalName() != null) {
                        resolvedValue = executionContext.resolveSymbol(valueAsVariable.getLocalName());
                    } else {
                        resolvedValue = valueAsVariable.getValue();
                    }
                }

                if (resolvedValue != null) {
                    if (resolvedValue instanceof PythonExpression) {
                        PythonExpression variableExpression = (PythonExpression) resolvedValue;
                        resolvedValue = run(variableExpression, scope);
                    } else if (resolvedValue instanceof PythonVariable) {
                        resolvedValue = ((PythonVariable) resolvedValue).getValue();
                    }
                }

                if (resolvedValue != null) {
                    asVariable.setValue(resolvedValue);
                } else {
                    asVariable.setValue(new PythonIndeterminateValue());
                }
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
