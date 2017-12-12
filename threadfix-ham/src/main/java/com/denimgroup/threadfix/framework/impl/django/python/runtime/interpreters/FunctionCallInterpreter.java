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

import static com.denimgroup.threadfix.CollectionUtils.map;

public class FunctionCallInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {

        FunctionCallExpression callExpression = (FunctionCallExpression)expression;
        ExecutionContext executionContext = host.getExecutionContext();

        PythonValue subject = callExpression.getSubject(0);
        subject = executionContext.resolveValue(subject);

        if (subject.getSourceLocation() == null) {
            return new PythonIndeterminateValue();
        }

        AbstractPythonStatement sourceLocation = subject.getSourceLocation();

        if (sourceLocation instanceof PythonClass) {

            PythonClass sourceClass = (PythonClass)sourceLocation;
            PythonObject newObject = new PythonObject(sourceClass);

            PythonCodeCollection codebase = executionContext.getCodebase();
            for (PythonPublicVariable exposedVars : sourceClass.findChildren(PythonPublicVariable.class)) {
                PythonValue initialValue = host.run(
                        new File(exposedVars.getSourceCodePath()),
                        exposedVars.getSourceCodeStartLine(),
                        exposedVars.getSourceCodeEndLine(),
                        sourceLocation,
                        newObject
                );

                newObject.setMemberValue(exposedVars.getName(), initialValue);
            }

            PythonFunction initFunction = sourceClass.findChild("__init__", PythonFunction.class);
            if (initFunction != null) {
                ExecutionContext invokeContext = new ExecutionContext(codebase, newObject, initFunction);
                invokeFunction(host, initFunction, callExpression.getParameters(), invokeContext);
            }

            return newObject;

        } else if (sourceLocation instanceof PythonFunction) {

            PythonFunction sourceFunction = (PythonFunction)sourceLocation;

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
                }
            }

            PythonCodeCollection codebase = executionContext.getCodebase();
            invokeContext = new ExecutionContext(codebase, invokeSelfValue, sourceFunction);

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

    private PythonValue invokeFunction(PythonInterpreter interpreter, PythonFunction function, List<PythonValue> parameters, ExecutionContext newContext) {
        List<String> paramNames = function.getParams();
        for (int i = 0; i < parameters.size() && i < paramNames.size(); i++) {
            String name = paramNames.get(i);
            PythonValue paramValue = parameters.get(i);

            newContext.assignSymbolValue(name, paramValue);
        }

        PythonValue result = interpreter.run(
                new File(function.getSourceCodePath()),
                function.getSourceCodeStartLine(),
                function.getSourceCodeEndLine()
        );

        return result;
    }
}
