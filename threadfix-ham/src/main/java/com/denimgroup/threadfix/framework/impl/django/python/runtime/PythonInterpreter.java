package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.util.CondensedLinesMap;
import com.denimgroup.threadfix.framework.util.FileReadUtils;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public class PythonInterpreter {


    private static final SanitizedLogger LOG = new SanitizedLogger(PythonInterpreter.class);


    ExecutionContext executionContext;
    PythonExpressionParser expressionParser;
    PythonValueBuilder valueBuilder = new PythonValueBuilder();

    int maxStackDepth = 15;

    //  Whether or not the interpreter should run raised scopes (ie 'if' statements within
    //      some logic)
    boolean enableInnerScopes = false;

    public PythonInterpreter(PythonCodeCollection codebase) {
        executionContext = new ExecutionContext(codebase);
        expressionParser = new PythonExpressionParser(codebase);

        this.executionContext.loadModuleDeclarations();
    }

    public PythonInterpreter(@Nonnull ExecutionContext executionContext) {
        this.executionContext = executionContext;
        expressionParser = new PythonExpressionParser(executionContext.getCodebase());

        this.executionContext.loadModuleDeclarations();
    }

    public PythonInterpreter(@Nonnull PythonCodeCollection codebase, PythonValue valueContext) {
        this.executionContext = new ExecutionContext(codebase, valueContext);
        expressionParser = new PythonExpressionParser(codebase);

        this.executionContext.loadModuleDeclarations();
    }


    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public ExecutionContext getRootExecutionContext() {
        ExecutionContext current = this.executionContext;
        while (current.parentContext != null) {
            current = current.parentContext;
        }
        return current;
    }



    public void setMaxStackDepth(int maxStackDepth) {
        this.maxStackDepth = maxStackDepth;
    }

    public int getMaxStackDepth() {
        return maxStackDepth;
    }



    public PythonValue run(@Nonnull String code) {
        return run(code, null, null);
    }

    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope) {
        return run(code, scope, null);
    }

    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope, PythonValue selfValue) {
        code = Language.stripComments(code);

        PythonExpression expression = expressionParser.processString(code, null, executionContext.getScope());
        if (expression instanceof IndeterminateExpression) {
            return valueBuilder.buildFromSymbol(code);
        } else {
            PythonValue evaluated = run(expression, scope, selfValue);
            if (!(evaluated instanceof PythonIndeterminateValue)) {
                return evaluated;
            } else {
                return valueBuilder.buildFromSymbol(code);
            }
        }
    }

    public PythonValue run(@Nonnull PythonExpression expression) {
        return run(expression, (AbstractPythonStatement) null);
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine) {
        return run(targetFile, startLine, endLine, null);
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine, AbstractPythonStatement scope) {
        return run(targetFile, startLine, endLine, scope, null);
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine, AbstractPythonStatement scope, PythonValue selfValue) {
        CondensedLinesMap lines = FileReadUtils.readLinesCondensed(targetFile.getAbsolutePath(), startLine, endLine);
        PythonValue lastValue = null;
        for (String line : lines.getCondensedLines()) {
            lastValue = run(line, scope, selfValue);
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

        if (usesNewContext) {
            pushExecutionContext(scope, selfValue);
        }

        PythonValue result = run(expression, this.executionContext);

        if (usesNewContext) {
            popExecutionContext();
        }

        if (result != null) {
            return result;
        } else {
            return new PythonIndeterminateValue();
        }
    }

    public PythonValue run(@Nonnull PythonExpression expression, ExecutionContext executionContext) {
        //  Real run function

        if (executionContext == null) {
            executionContext = this.executionContext;
        }

        int currentStackDepth = executionContext.getStackDepth();
        if (currentStackDepth >= getMaxStackDepth()) {
            LOG.warn("Execution context stack size '" + currentStackDepth +
                    "' exceeded the maximum support size '" + getMaxStackDepth() +
                    "', prematurely terminating current expression: " +
                    expression.toString());
            return new PythonIndeterminateValue();
        }

        if (expression.getScopingIndentation() > executionContext.getPrimaryScopeLevel()) {
            return new PythonIndeterminateValue();
        }

        ExpressionInterpreter interpreter = expression.makeInterpreter();
        if (interpreter == null) {
            return new PythonIndeterminateValue();
        } else {
            boolean usesNewContext = false;
            if (executionContext != this.executionContext) {
                pushExecutionContext(executionContext);
                usesNewContext = true;
            }

            AbstractPythonStatement currentScope = executionContext.getScope();
            PythonCodeCollection codebase = executionContext.getCodebase();

            if (currentScope != null && codebase != null) {
                InterpreterUtil.resolveSourceLocations(expression, executionContext.getScope(), executionContext.getCodebase());
            }

            resolveDependencies(expression, currentScope);

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
                PythonValue resolvedValue = run((PythonExpression) subValue, scope, executionContext.getSelfValue());
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
                    } else if (resolvedValue instanceof PythonVariable && ((PythonVariable) resolvedValue).getValue() != null) {
                        resolvedValue = ((PythonVariable) resolvedValue).getValue();
                    }
                }

                if (resolvedValue != null) {
                    asVariable.setValue(resolvedValue);
                } else {
                    if (asVariable.getSourceLocation() == null) {
                        asVariable.setValue(new PythonIndeterminateValue());
                    }
                }
            }
        }
    }



    public void pushExecutionContext(AbstractPythonStatement scope, PythonValue selfValue) {
        ExecutionContext newContext = new ExecutionContext(this.executionContext.codebase, selfValue, scope);
        if (scope != null) {
            //  For functions, current scope should be set to the indentation of the function's body
            if (scope instanceof PythonFunction && scope.getChildStatements().size() > 0) {
                newContext.setPrimaryScopeLevel(scope.getChildStatements().get(0).getIndentationLevel());
            } else {
                newContext.setPrimaryScopeLevel(scope.getIndentationLevel());
            }
        }
        newContext.parentContext = this.executionContext;
        this.executionContext = newContext;
    }

    public void pushExecutionContext(ExecutionContext executionContext) {
        if (this.executionContext != executionContext) {
            executionContext.parentContext = this.executionContext;
            this.executionContext = executionContext;
        }
    }

    public void popExecutionContext() {
        if (this.executionContext != null && this.executionContext.parentContext != null) {
            this.executionContext = this.executionContext.parentContext;
        }
    }

}
