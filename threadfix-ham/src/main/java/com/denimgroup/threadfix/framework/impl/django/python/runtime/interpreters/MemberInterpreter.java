package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.MemberExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

import java.util.List;

public class MemberInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        MemberExpression memberExpression = (MemberExpression)expression;
        ExecutionContext executionContext = host.getExecutionContext();
        PythonCodeCollection codebase = executionContext.getCodebase();

        PythonValue subject = memberExpression.getSubject(0);
        PythonValue resolvedSubject = executionContext.resolveAbsoluteValue(subject);

        AbstractPythonStatement subjectSource;
        if (resolvedSubject != null)
            subjectSource = resolvedSubject.getSourceLocation();
        else
            subjectSource = subject.getSourceLocation();

        //  Try to directly resolve it as a PythonObject
        if (resolvedSubject != null && resolvedSubject instanceof PythonObject) {
            PythonObject currentObject = (PythonObject) resolvedSubject;
            PythonValue selectedValue = null;
            AbstractPythonStatement lastSource = currentObject.getSourceLocation();
            for (String member : memberExpression.getMemberPath()) {
                if (currentObject == null) {
                    break;
                }
                if (!currentObject.hasMemberValue(member)) {
                    PythonVariable newVar = new PythonVariable(member, (PythonValue)null);
                    PythonObject nextObject = null;

                    AbstractPythonStatement memberSource = codebase.resolveLocalSymbol(member, lastSource);
                    if (memberSource != null) {
                        newVar.resolveSourceLocation(memberSource);
                        if (memberSource instanceof PythonPublicVariable) {
                            PythonPublicVariable asSchemaVar = (PythonPublicVariable)memberSource;
                            if (asSchemaVar.getResolvedTypeClass() != null) {
                                nextObject = new PythonObject(asSchemaVar.getResolvedTypeClass(), member);
                                newVar.setValue(nextObject);
                            }
                        }
                    }

                    currentObject.setRawMemberValue(member, executionContext.resolveAbsoluteValue(newVar), memberSource);
                    selectedValue = currentObject.getMemberValue(member);
                    currentObject = nextObject;
                } else {
                    selectedValue = currentObject.getMemberValue(member);
                    if (selectedValue == null) {
                        selectedValue = currentObject.getMemberVariable(member);
                    }

                    if (selectedValue instanceof PythonObject) {
                        currentObject = (PythonObject) selectedValue;
                    } else {
                        currentObject = null;
                    }
                }

                if (currentObject != null) {
                    lastSource = currentObject.getSourceLocation();
                }
            }

            if (selectedValue == null ||
               (selectedValue.getSourceLocation() != null && selectedValue.getSourceLocation().getFullName().endsWith(memberExpression.getFullMemberPath()))) {
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
                //  It may be an absolute reference
                if (currentValue != null) {
                    return currentValue;
                } else if ( currentStatement != null) {
                    PythonVariable result = new PythonVariable(currentStatement.getFullName());
                    result.resolveSourceLocation(currentStatement);
                    return result;
                }
            }

            if (currentStatement == null) {
                //  The symbol isn't in the codebase (as far as we know)
                return new PythonIndeterminateValue();
            } else if (currentValue != null) {
                currentValue = executionContext.resolveAbsoluteValue(currentValue);
                if (currentValue != null && currentValue instanceof PythonObject) {

                    List<String> remainingParts = memberPath.subList(numSkippedPaths, memberPath.size());
                    PythonObject asObject = (PythonObject)currentValue;

                    PythonVariable currentMemberVar = null;
                    for (int i = 0; i < remainingParts.size(); i++) {
                        String part = remainingParts.get(i);
                        currentMemberVar = asObject.getMemberVariable(part);
                        if (currentMemberVar == null) {

                        }
                        if (currentMemberVar == null || (!currentMemberVar.isType(PythonObject.class) && i != remainingParts.size() - 1)) {
                            currentMemberVar = null;
                            asObject = null;
                            break;
                        } else if (currentMemberVar.isType(PythonObject.class)) {
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
