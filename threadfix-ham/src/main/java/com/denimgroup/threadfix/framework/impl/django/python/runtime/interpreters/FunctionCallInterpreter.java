package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.FunctionCallExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class FunctionCallInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {

        //  TODO - Support Named Parameters

        FunctionCallExpression callExpression = (FunctionCallExpression)expression;
        ExecutionContext executionContext = host.getExecutionContext();

        PythonValue subject = callExpression.getSubject(0);

        String functionName = tryGetFunctionName(subject);
        AbstractPythonStatement statement = null;
        if (functionName != null) {
            statement = executionContext.findSymbolDeclaration(functionName);
        }

        if (statement == null) {
            return new PythonIndeterminateValue();
        }

        if (statement instanceof PythonClass) {

            PythonClass sourceClass = (PythonClass)statement;
            PythonObject newObject = new PythonObject(sourceClass);

            PythonCodeCollection codebase = executionContext.getCodebase();
            for (PythonPublicVariable exposedVars : sourceClass.findChildren(PythonPublicVariable.class)) {
                PythonValue initialValue = host.run(
                        new File(exposedVars.getSourceCodePath()),
                        exposedVars.getSourceCodeStartLine(),
                        exposedVars.getSourceCodeEndLine(),
                        statement,
                        newObject
                );

                newObject.setMemberValue(exposedVars.getName(), initialValue);
            }

            PythonFunction initFunction = sourceClass.findChild("__init__", PythonFunction.class);
            if (initFunction != null) {
                ExecutionContext invokeContext = new ExecutionContext(codebase, newObject, initFunction);
                int scopeLevel = initFunction.getIndentationLevel();
                if (initFunction.getChildStatements().size() > 0) {
                    scopeLevel = initFunction.getChildStatements().get(0).getIndentationLevel();
                }
                invokeContext.setPrimaryScopeLevel(scopeLevel);
                invokeFunction(host, initFunction, callExpression.getParameters(), invokeContext);
            }

            return newObject;

        } else if (statement instanceof PythonFunction) {

            PythonFunction sourceFunction = (PythonFunction)statement;

            PythonValue invokeSelfValue = null;
            ExecutionContext invokeContext;
            if (subject instanceof PythonObject) {
                invokeSelfValue = subject;
            } else if (subject instanceof PythonVariable) {
                PythonVariable subjectAsVariable = (PythonVariable)subject;
                if (subjectAsVariable.getValue() != null) {
                    if (subjectAsVariable.getValue() instanceof PythonObject) {
                        invokeSelfValue = subjectAsVariable.getValue();
                    } else if (subjectAsVariable.getOwner() != null) {
                        PythonValue owner = subjectAsVariable.getOwner();
                        if (owner instanceof PythonObject) {
                            invokeSelfValue = owner;
                        }
                    }
                } else if (subjectAsVariable.getOwner() != null) {
                    PythonValue owner = subjectAsVariable.getOwner();
                    if (owner instanceof PythonObject) {
                        invokeSelfValue = owner;
                    }
                }
            }

            PythonCodeCollection codebase = executionContext.getCodebase();
            invokeContext = new ExecutionContext(codebase, invokeSelfValue, sourceFunction);
            int indentationScope = sourceFunction.getIndentationLevel();
            if (sourceFunction.getChildStatements().size() > 0) {
                indentationScope = sourceFunction.getChildStatements().get(0).getIndentationLevel();
            }

            invokeContext.setPrimaryScopeLevel(indentationScope);

            PythonValue result = null;
            result = invokeFunction(host, sourceFunction, callExpression.getParameters(), invokeContext);

            if (result == null) {
                return new PythonIndeterminateValue();
            } else {
                return result;
            }

        } else {
            //  TODO - Need to support variables that are assigned to functions
            return new PythonIndeterminateValue();
        }
    }

    private List<PythonValue> reorderParameters(PythonFunction targetFunction, List<PythonValue> enumeratedParameters) {

        if (targetFunction.getParams().size() == 0) {
            return enumeratedParameters;
        }

        Map<String, PythonValue> mappedParams = map();
        List<String> functionParams = targetFunction.getParams();

        for (PythonValue param : enumeratedParameters) {
            if (!(param instanceof PythonVariable)) {
                String paramName = functionParams.get(mappedParams.size());
                mappedParams.put(paramName, param);
            } else {
                String varName = ((PythonVariable) param).getLocalName();
                if (functionParams.contains(varName)) {
                    mappedParams.put(varName, param);
                } else {
                    String paramName = functionParams.get(mappedParams.size());
                    mappedParams.put(paramName, param);
                }
            }
        }

        List<PythonValue> result = list();
        for (String paramName : functionParams) {
            result.add(mappedParams.get(paramName));
        }
        return result;
    }

    private PythonValue invokeFunction(PythonInterpreter interpreter, PythonFunction function, List<PythonValue> parameters, ExecutionContext newContext) {

        parameters = reorderParameters(function, parameters);
        PythonValue result = null;

        interpreter.pushExecutionContext(newContext);

        if (function.canInvoke()) {
            PythonValue[] paramsArray = new PythonValue[parameters.size()];
            paramsArray = parameters.toArray(paramsArray);
            result = function.invoke(interpreter, newContext.getScope(), paramsArray);

        } else {

            //  TODO - Assign default param values from function signature

            List<String> paramNames = function.getParams();
            for (int i = 0; i < parameters.size() && i < paramNames.size(); i++) {
                String name = paramNames.get(i);
                if (name.equals("self")) {
                    continue;
                }

                PythonValue paramValue = parameters.get(i);

                newContext.assignSymbolValue(name, paramValue);
            }

            result = interpreter.run(
                    new File(function.getSourceCodePath()),
                    function.getSourceCodeStartLine(),
                    function.getSourceCodeEndLine(),
                    newContext.getScope(),
                    newContext.getSelfValue()
            );
        }

        interpreter.popExecutionContext();

        return result;
    }

    private String tryGetFunctionName(PythonValue possibleFunction) {
        PythonValue subject = null;
        String subjectPath = null;
        if (possibleFunction instanceof PythonUnaryExpression && ((PythonUnaryExpression) possibleFunction).numSubjects() > 0) {
            subject = ((PythonUnaryExpression) possibleFunction).getSubject(0);
            if (subject.getSourceLocation() != null) {
                subjectPath = subject.getSourceLocation().getFullName();
            }
        } else if (possibleFunction instanceof PythonVariable) {
            if (possibleFunction.getSourceLocation() != null) {
                subjectPath = possibleFunction.getSourceLocation().getFullName();
            } else {
                PythonValue varValue = ((PythonVariable) possibleFunction).getValue();
                if (varValue != null) {
                    subjectPath = tryGetFunctionName(varValue);
                }
            }
        }

        String functionName = null;

        if (possibleFunction instanceof PythonVariable) {
            String varName = ((PythonVariable) possibleFunction).getLocalName();
            if (varName != null) {
                if (subjectPath != null) {
                    if (!subjectPath.endsWith(varName)) {
                        functionName = subjectPath + "." + varName;
                    } else {
                        functionName = subjectPath;
                    }
                } else {
                    functionName = varName;
                }
            } else if (subjectPath != null) {
                functionName = subjectPath;
            }
        }

        return functionName;
    }
}
