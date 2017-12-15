package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.MemberExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;

import java.util.List;

public class MemberInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        MemberExpression memberExpression = (MemberExpression)expression;
        ExecutionContext executionContext = host.getExecutionContext();
        PythonCodeCollection codebase = executionContext.getCodebase();

        PythonValue subject = memberExpression.getSubject(0);
        PythonValue resolvedSubject = executionContext.resolveValue(subject);

        AbstractPythonStatement subjectSource = subject.getSourceLocation();

        //  Try to directly resolve it as a PythonObject
        if (resolvedSubject != null && resolvedSubject instanceof PythonObject) {
            PythonObject currentObject = (PythonObject) resolvedSubject;
            PythonValue selectedValue = null;
            for (String member : memberExpression.getMemberPath()) {
                if (currentObject == null) {
                    break;
                }
                if (!currentObject.hasMemberValue(member)) {
                    return new PythonIndeterminateValue();
                } else {
                    selectedValue = currentObject.getMemberValue(member);
                    if (selectedValue instanceof PythonObject) {
                        currentObject = (PythonObject) selectedValue;
                    } else {
                        currentObject = null;
                    }
                }
            }

            if (selectedValue == null) {
                return new PythonIndeterminateValue();
            } else {
                return selectedValue;
            }
        } else if (subjectSource != null && subjectSource instanceof PythonModule) {
            //  Symbol may include a module path
            List<String> memberPath = memberExpression.getMemberPath();
            AbstractPythonStatement currentStatement = subjectSource;
            PythonValue currentValue = null;
            int numSkippedPaths = 0;
            for (String path : memberPath) {
                currentStatement = currentStatement.findChild(path);
                if (currentStatement == null) {
                    break;
                }
                ++numSkippedPaths;
                currentValue = executionContext.resolveSymbol(currentStatement.getFullName());
                if (currentValue != null && (currentValue instanceof PythonVariable || currentValue instanceof PythonObject)) {
                    break;
                } else {
                    currentValue = null;
                }
            }

            if (numSkippedPaths == memberPath.size()) {
                return currentValue;
            }

            if (currentStatement == null) {
                //  The symbol isn't in the codebase (as far as we know)
                return new PythonIndeterminateValue();
            } else if (currentValue != null) {
                currentValue = executionContext.resolveValue(currentValue);
                if (currentValue != null && currentValue instanceof PythonObject) {

                    List<String> remainingParts = memberPath.subList(numSkippedPaths, memberPath.size());
                    PythonObject asObject = (PythonObject)currentValue;

                    PythonVariable currentMemberVar = null;
                    for (String part : remainingParts) {
                        currentMemberVar = asObject.getMemberVariable(part);
                        if (!currentMemberVar.isType(PythonObject.class)) {
                            currentMemberVar = null;
                            asObject = null;
                            break;
                        } else {
                            asObject = (PythonObject)currentMemberVar.getValue();
                        }
                    }

                    if (currentMemberVar != null) {
                        return currentMemberVar;
                    } else if (asObject != null) {
                        return asObject;
                    } else {
                        return new PythonIndeterminateValue();
                    }

                } else {
                    return new PythonIndeterminateValue();
                }
            } else {
                return new PythonIndeterminateValue();
            }
        } else {
            return new PythonIndeterminateValue();
        }
    }
}
