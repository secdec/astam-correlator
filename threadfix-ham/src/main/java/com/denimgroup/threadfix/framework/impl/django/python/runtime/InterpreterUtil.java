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

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.FunctionCallExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class InterpreterUtil {

    private static final PythonExpressionParser sharedParser = new PythonExpressionParser();
    private static final PythonValueBuilder sharedBuilder = new PythonValueBuilder();

    public static String tryGetValueSymbol(PythonValue value) {
        if (value.getSourceLocation() != null) {
            return value.getSourceLocation().getFullName();
        } else if (value instanceof PythonObject) {
            return ((PythonObject) value).getMemberPath();
        } else if (value instanceof PythonVariable) {
            return ((PythonVariable) value).getLocalName();
        } else {
            return null;
        }
    }

    public static boolean testEquality(PythonValue lhs, PythonValue rhs) {
        if (lhs == rhs) {
            return true;
        }

        if (lhs instanceof PythonVariable) {
            lhs = ((PythonVariable)lhs).getValue();
        }

        if (rhs instanceof PythonVariable) {
            rhs = ((PythonVariable)rhs).getValue();
        }

        if (lhs == rhs) {
            return true;
        }

        if (lhs.getClass() != rhs.getClass()) {
            return false;
        } else {
            if (lhs instanceof PythonObject) {
                return false; // Python objects can only be compared by reference
            } else if (lhs instanceof PythonArray) {
                return ((PythonArray)lhs).testEquality((PythonArray)rhs);
            } else if (lhs instanceof PythonDictionary) {
                return ((PythonDictionary)lhs).testEquality((PythonDictionary)rhs);
            } else if (lhs instanceof PythonNumericPrimitive) {
                return ((PythonNumericPrimitive)lhs).getValue() == ((PythonNumericPrimitive)rhs).getValue();
            } else if (lhs instanceof PythonStringPrimitive) {
                return ((PythonStringPrimitive)lhs).getValue().equals(((PythonStringPrimitive)rhs).getValue());
            } else if (lhs instanceof PythonNone) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static AbstractPythonStatement tryGetSource(PythonValue value) {
        AbstractPythonStatement source = null;
        while (value instanceof PythonVariable) {
            PythonVariable asVar = (PythonVariable)value;
            if (asVar.getSourceLocation() != null) {
                source = asVar.getSourceLocation();
            }
            value = asVar.getValue();
        }
        return source;
    }

    public static PythonValue tryMakeValue(String expression, List<PythonValue> expressionSubject) {
        PythonValue asValue = sharedBuilder.buildFromSymbol(expression);
        if (isValidValue(asValue)) {
            return asValue;
        } else {
            return sharedParser.processString(expression, expressionSubject);
        }
    }

    public static boolean isValidValue(PythonValue value) {
        return value != null && !(value instanceof PythonIndeterminateValue);
    }

    public static boolean isValidExpression(PythonExpression expression) {
        return expression != null && !(expression instanceof IndeterminateExpression);
    }

    public static void resolveSubValues(PythonValue value) {
        List<PythonValue> subValues = value.getSubValues();
        if (subValues == null) {
            return;
        } else {
            subValues = new LinkedList<PythonValue>(value.getSubValues());
        }
        while (subValues.size() > 0) {
            PythonValue subValue = subValues.get(0);
            if (subValue instanceof PythonUnresolvedValue) {
                PythonUnresolvedValue unresolvedValue = (PythonUnresolvedValue)subValue;
                PythonValue resolvedValue = tryMakeValue(unresolvedValue.getStringValue(), null);
                if (!(resolvedValue instanceof PythonUnresolvedValue)) {
                    resolvedValue.resolveSourceLocation(unresolvedValue.getSourceLocation());
                    value.resolveSubValue(subValue, resolvedValue);
                    subValue = resolvedValue;
                }
            }

            resolveSubValues(subValue);
            subValues.remove(0);
        }
    }

    public static void resolveSourceLocations(@Nonnull PythonValue value, AbstractPythonStatement scope, @Nonnull PythonCodeCollection codebase) {

        AbstractPythonStatement source = null;
        if (value.getSourceLocation() == null) {
            if (value instanceof PythonVariable) {
                String name = ((PythonVariable) value).getLocalName();
                if (name != null) {
                    if (scope != null) {
                        source = codebase.resolveLocalSymbol(name, scope);
                    } else {
                        source = codebase.findByFullName(name);
                    }
                    PythonValue variableValue = ((PythonVariable) value).getValue();
                    if (source != null) {
                        if (variableValue != null) {
                            if (variableValue instanceof PythonObject && source instanceof PythonPublicVariable) {
                                PythonClass valueType = ((PythonPublicVariable) source).getResolvedTypeClass();
                                variableValue.resolveSourceLocation(valueType);
                                ((PythonObject) variableValue).setClassType(valueType);
                            } else if (variableValue instanceof FunctionCallExpression) {
                                variableValue.resolveSourceLocation(source);
                            }
                        }
                    }
                }
            } else if (value instanceof PythonObject) {

            } else if (value instanceof FunctionCallExpression && ((FunctionCallExpression) value).getSubject(0) instanceof PythonVariable) {
                PythonVariable subject = (PythonVariable)((FunctionCallExpression) value).getSubject(0);
                resolveSourceLocations(subject, scope, codebase);
                source = subject.getSourceLocation();
            }
        }

        if (source != null) {
            value.resolveSourceLocation(source);
        }

        Collection<PythonValue> subValues = value.getSubValues();
        if (subValues != null) {
            for (PythonValue subValue : subValues) {
                resolveSourceLocations(subValue, scope, codebase);
            }
        }
    }



    public static boolean expressionContains(PythonValue expression, PythonValue searchValue) {
        if (expression == searchValue) {
            return true;
        } else {
            List<PythonValue> subValues = expression.getSubValues();
            if (subValues != null) {
                for (PythonValue subValue : subValues) {
                    if (expressionContains(subValue, searchValue)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }



    public static int countSpacingLevel(String code) {
        int spacing = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == ' ') {
                spacing++;
            } else if (c == '\t') {
                spacing += 4;
            } else if (c != '\n') {
                break;
            }
        }
        return spacing;
    }
}
