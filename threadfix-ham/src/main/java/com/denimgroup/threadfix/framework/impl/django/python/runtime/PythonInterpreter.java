////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.CondensedLinesMap;
import com.denimgroup.threadfix.framework.impl.django.python.PythonFileReadUtils;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PythonInterpreter {


    private static final SanitizedLogger LOG = new SanitizedLogger(PythonInterpreter.class);


    ExecutionContext executionContext;
    PythonExpressionParser expressionParser;
    PythonValueBuilder valueBuilder = new PythonValueBuilder();
    Stack<PythonValue> currentDependencyChain = new Stack<PythonValue>();


    // Prevents stack overflow exceptions and wasted cycles processing an infinitely-recursive expression
    int maxStackDepth = 50;

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
        while (current.getParentContext() != null) {
            current = current.getParentContext();
        }
        return current;
    }



    public void setMaxStackDepth(int maxStackDepth) {
        this.maxStackDepth = maxStackDepth;
    }

    public int getMaxStackDepth() {
        return maxStackDepth;
    }


    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope, PythonValue selfValue) {
        code = Language.stripComments(code);

        ExecutionContext newContext = new ExecutionContext(executionContext.getCodebase(), selfValue, scope);
        return run(code, newContext);
    }

    public PythonValue run(@Nonnull String code, AbstractPythonStatement scope, PythonValue selfValue, Map<String, PythonValue> params) {
        ExecutionContext newContext = new ExecutionContext(executionContext.getCodebase(), selfValue, scope);
        for (Map.Entry<String, PythonValue> param : params.entrySet()) {
            newContext.assignSymbolValue(param.getKey(), param.getValue());
        }

        pushExecutionContext(newContext);
        PythonValue result = run(code, newContext);
        popExecutionContext();
        return result;
    }

    public PythonValue run(@Nonnull File targetFile, int startLine, int endLine, AbstractPythonStatement scope, PythonValue selfValue) {
        pushExecutionContext(scope, selfValue);
        ExecutionContext tempContext = getExecutionContext();

        CondensedLinesMap lines = PythonFileReadUtils.readLinesCondensed(targetFile.getAbsolutePath(), startLine, endLine);
        PythonValue lastValue = null;
        PythonValue returnValue = null;
        for (String line : lines.getCondensedLines()) {
            lastValue = run(line, tempContext);
            if (line.trim().startsWith("return")) {
                returnValue = lastValue;
            }
        }

        popExecutionContext();
        return returnValue != null ? returnValue : lastValue;
    }

    public PythonValue run(@Nonnull PythonExpression expression, AbstractPythonStatement scope, PythonValue selfValue) {

        boolean usesNewContext =
                selfValue != this.executionContext.getSelfValue() ||
                        scope != this.executionContext.getScope();

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

    public PythonValue run(@Nonnull String code, ExecutionContext executionContext) {
        if (executionContext == null) {
            executionContext = this.executionContext;
        }
        PythonExpression expression = expressionParser.processString(code, null, executionContext.getScope());
        if (expression instanceof IndeterminateExpression) {
            return valueBuilder.buildFromSymbol(code);
        } else {
            PythonValue evaluated = run(expression, executionContext);
            if (!(evaluated instanceof PythonIndeterminateValue)) {
                return evaluated;
            } else {
                return valueBuilder.buildFromSymbol(code);
            }
        }
    }

    public PythonValue run(@Nonnull PythonExpression expression, ExecutionContext executionContext) {
        //  Real run function

        if (executionContext == null) {
            executionContext = this.executionContext;
        }

        int currentStackDepth = executionContext.getStackDepth();
        if (currentStackDepth >= getMaxStackDepth()) {
            LOG.debug("Execution context stack size '" + currentStackDepth +
                    "' exceeded the maximum support size '" + getMaxStackDepth() +
                    "', prematurely terminating current expression: " +
                    expression.toString());
            return new PythonIndeterminateValue();
        }

        if (!enableInnerScopes && expression.getScopingIndentation() > executionContext.getPrimaryScopeLevel()) {
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

            if (codebase != null) {
                InterpreterUtil.resolveSourceLocations(expression, executionContext.getScope(), executionContext.getCodebase());
            }

            resolveDependencies(expression, currentScope);

            PythonValue result = null;

            int currentDependencyDepth = currentDependencyChain.size();

            try {
                result = interpreter.interpret(this, expression);
            } catch (StackOverflowError soe) {
                LOG.warn("Stack overflow occurred while executing python interpreter," +
                        " prematurely terminating current expression: " + expression.toString());
            }

            //  Dependency chain may contain old values if an exception occurred while interpreting
            while (currentDependencyDepth > currentDependencyChain.size()) {
                currentDependencyChain.pop();
            }

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

    private void resolveDependencies(PythonValue expression, AbstractPythonStatement scope) {

        boolean pushedDependencyChain = false;

        try {

            if (expression == null) {
                return;
            }

            List<PythonValue> dependencies = expression.getSubValues();
            if (dependencies == null) {
                return;
            }

            //  Terminate circular dependencies ie foo = bar(foo)
            if (currentDependencyChain.contains(expression)) {
                return;
            }

            currentDependencyChain.push(expression);
            pushedDependencyChain = true;

            for (PythonValue subValue : dependencies) {
                if (currentDependencyChain.contains(subValue)) {
                    continue;
                }

                if (subValue instanceof PythonExpression) {
                    PythonValue resolvedValue = run((PythonExpression) subValue, scope, executionContext.getSelfValue());
                    expression.resolveSubValue(subValue, resolvedValue);
                } else if (subValue instanceof PythonVariable) {
                    PythonVariable asVariable = (PythonVariable) subValue;

                    if (asVariable.getValue() == null && asVariable.getLocalName() != null) {
                        PythonValue resolvedValue = executionContext.resolveSymbol(asVariable.getLocalName());
                        //  Prevent circular dependencies
                        if (resolvedValue != null &&
                                !InterpreterUtil.expressionContains(asVariable, resolvedValue) &&
                                ((resolvedValue.getSourceLocation() != asVariable.getSourceLocation()) ||
                                        (resolvedValue.getSourceLocation() == null && asVariable.getSourceLocation() == null))) {
                            asVariable.setRawValue(executionContext.resolveAbsoluteValue(resolvedValue));
                        }
                    }

                    PythonValue resolvedValue = asVariable.getValue();
                    resolvedValue = executionContext.resolveAbsoluteValue(resolvedValue);

                    if (resolvedValue != null) {
                        if (resolvedValue instanceof PythonExpression) {
                            PythonExpression variableExpression = (PythonExpression) resolvedValue;
                            resolvedValue = run(variableExpression, scope, executionContext.getSelfValue());
                        } else if (resolvedValue instanceof PythonVariable && ((PythonVariable) resolvedValue).getValue() != null) {
                            resolvedValue = ((PythonVariable) resolvedValue).getValue();
                        }
                    }

                    if (resolvedValue != null) {
                        if (resolvedValue instanceof PythonVariable) {
                            expression.resolveSubValue(subValue, resolvedValue);
                        } else {
                            asVariable.setValue(resolvedValue);
                        }
                    } else {
                        if (asVariable.getSourceLocation() == null) {
                            asVariable.setValue(new PythonIndeterminateValue());
                        }
                    }
                }
            }

            if (expression.getSubValues() != null) {
                // Run in a separate loop to resolve dependencies on expressions
                //  that were just resolved
                for (PythonValue subValue : expression.getSubValues()) {
                    if (!currentDependencyChain.contains(subValue)) {
                        resolveDependencies(subValue, scope);
                    }
                }
            }

        } catch (StackOverflowError soe) {
            LOG.warn("Stack overflow occurred while resolving python expression dependencies");
        } finally {
            if (pushedDependencyChain) {
                currentDependencyChain.pop();
            }
        }
    }



    public void pushExecutionContext(AbstractPythonStatement scope, PythonValue selfValue) {
        ExecutionContext newContext = new ExecutionContext(this.executionContext.getCodebase(), selfValue, scope);
        updatePrimaryScopeLevel(scope, newContext);
        newContext.setParentContext(this.executionContext);
        this.executionContext = newContext;
    }

    public void pushExecutionContext(ExecutionContext executionContext) {
        if (this.executionContext != executionContext) {
            updatePrimaryScopeLevel(executionContext.getScope(), executionContext);
            executionContext.setParentContext(this.executionContext);
            this.executionContext = executionContext;
        }
    }

    public void popExecutionContext() {
        if (this.executionContext != null && this.executionContext.getParentContext() != null) {
            this.executionContext = this.executionContext.getParentContext();
        }
    }

    private void updatePrimaryScopeLevel(AbstractPythonStatement newScope, ExecutionContext newContext) {
        if (newScope == null || newContext == null) {
            return;
        }

        //  For functions and classes, current scope should be set to the indentation of the function's body
        if ((newScope instanceof PythonFunction || newScope instanceof PythonClass) && newScope.getChildStatements().size() > 0) {
            newContext.setPrimaryScopeLevel(newScope.getChildStatements().get(0).getIndentationLevel());
        } else {
            newContext.setPrimaryScopeLevel(newScope.getIndentationLevel());
        }
    }

}
