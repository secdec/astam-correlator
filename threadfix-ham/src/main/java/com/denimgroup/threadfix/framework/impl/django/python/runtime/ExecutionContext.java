package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.Language;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class ExecutionContext {

    PythonCodeCollection codebase;
    Map<String, PythonValue> workingMemory = map();
    PythonValue selfValue;
    AbstractPythonStatement scope;
    ExecutionContext parentContext = null;

    public ExecutionContext(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue) {
        this.codebase = codebase;
        setSelfValue(selfValue);
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue, AbstractPythonStatement scope) {
        this.codebase = codebase;
        this.scope = scope;
        setSelfValue(selfValue);
    }

    public PythonValue getSelfValue() {
        return selfValue;
    }

    public void setSelfValue(PythonValue selfValue) {
        this.selfValue = selfValue;
        this.workingMemory.put("self", selfValue);
    }

    public Map<String, PythonValue> getWorkingMemory() {
        return workingMemory;
    }

    public AbstractPythonStatement getScope() {
        return scope;
    }

    public void setScope(AbstractPythonStatement scope) {
        this.scope = scope;
    }

    public ExecutionContext getParentContext() {
        return parentContext;
    }

    public void setParentContext(ExecutionContext parentContext) {
        this.parentContext = parentContext;
    }

    public PythonValue resolveSymbol(String symbolName) {
        if (scope != null) {
            AbstractPythonStatement codebaseLocation = codebase.resolveLocalSymbol(symbolName, scope);
            if (codebaseLocation != null) {
                String fullName = codebaseLocation.getFullName();
                return findSymbol(fullName);
            }
        }

        return findSymbol(symbolName);
    }

    public PythonValue resolveValue(PythonValue value) {
        if (value instanceof PythonVariable) {
            PythonVariable asVariable = (PythonVariable)value;
            PythonValue result = asVariable.getValue();
            if (result == null && workingMemory.containsKey(asVariable.getLocalName())) {
                return resolveValue(workingMemory.get(asVariable.getLocalName()));
            } else {
                return result;
            }
        } else {
            return value;
        }
    }

    public void assignSymbolValue(String symbolName, PythonValue newValue) {
        AbstractPythonStatement codebaseLocation;
        if (scope == null) {
            codebaseLocation = codebase.findByFullName(symbolName);
        } else {
            codebaseLocation = codebase.resolveLocalSymbol(symbolName, scope);
        }
        if (codebaseLocation != null) {
            symbolName = codebaseLocation.getFullName();
        }

        //  Search parent contexts for existing entry for this symbol
        Map<String, PythonValue> valueMap = null;
        ExecutionContext currentContext = this;
        PythonValue currentValue = null;
        while (currentContext!= null) {
            if (currentContext.workingMemory.containsKey(symbolName)) {
                valueMap = currentContext.workingMemory;
                currentValue = valueMap.get(symbolName);
                break;
            }
            currentContext = currentContext.parentContext;
        }

        if (valueMap == null) {
            valueMap = this.workingMemory;
        }

        if (newValue != null) {
            if (currentValue != null && (currentValue instanceof PythonVariable)) {
                ((PythonVariable) currentValue).setValue(newValue);
            } else if (valueMap.containsKey(symbolName)) {
                valueMap.put(symbolName, newValue);
            } else {
                valueMap.put(symbolName, new PythonVariable(symbolName, newValue));
            }
        } else if (valueMap.containsKey(symbolName)) {
            valueMap.remove(symbolName);
        }
    }

    public PythonCodeCollection getCodebase() {
        return codebase;
    }

    public void loadModuleDeclarations() {
        final PythonExpressionParser expressionParser = new PythonExpressionParser();
        final Map<String, PythonValue> workingMemory = this.workingMemory;
        final PythonCodeCollection codebase = this.codebase;
        final PythonValueBuilder valueBuilder = new PythonValueBuilder();

        codebase.traverse(new AbstractPythonVisitor() {
            @Override
            public void visitModule(PythonModule pyModule) {
                super.visitModule(pyModule);

                for (AbstractPythonStatement child : pyModule.getChildStatements(PythonPublicVariable.class, PythonVariableModification.class)) {
                     String fullSymbol = null;
                    String assignedValue = null;

                    if (child instanceof PythonPublicVariable) {
                        fullSymbol = child.getFullName();
                        assignedValue = ((PythonPublicVariable) child).getValueString();
                    } else if (child instanceof PythonVariableModification) {
                        fullSymbol = ((PythonVariableModification) child).getTarget();
                        AbstractPythonStatement resolvedTarget = codebase.resolveLocalSymbol(fullSymbol, child);
                        if (resolvedTarget != null && resolvedTarget instanceof PythonPublicVariable) {
                            fullSymbol = resolvedTarget.getFullName();
                            assignedValue = ((PythonPublicVariable) resolvedTarget).getValueString();
                        }
                    }

                    if (fullSymbol == null) {
                        continue;
                    }

                    if (fullSymbol.contains("__init__")) {
                        fullSymbol = fullSymbol.replace(".__init__", "");
                    }

                    AbstractPythonStatement resolvedValueStatement = null;
                    if (assignedValue != null) {
                        resolvedValueStatement = codebase.resolveLocalSymbol(assignedValue, child);
                        if (resolvedValueStatement != null) {
                            assignedValue = resolvedValueStatement.getFullName();
                        } else if (assignedValue.contains("(")) {
                            String referencedFunctionSymbol;
                            referencedFunctionSymbol = assignedValue.substring(0, assignedValue.indexOf('('));
                            resolvedValueStatement = codebase.resolveLocalSymbol(referencedFunctionSymbol, child);
                            if (resolvedValueStatement != null) {
                                String remainingValuePart = assignedValue.substring(assignedValue.indexOf('('));
                                assignedValue = resolvedValueStatement.getFullName() + remainingValuePart;
                            }
                        }
                    }

                    PythonValue parsedAssignedValue = null;

                    if (assignedValue != null) {
                        assignedValue = StringUtils.replace(assignedValue, "\n", "").trim();

                        if (assignedValue.length() == 0) {
                            assignedValue = "None";
                        }

                        parsedAssignedValue = InterpreterUtil.tryMakeValue(assignedValue, null);

                        InterpreterUtil.resolveSubValues(parsedAssignedValue);
                    }

                    PythonValue existingValue = workingMemory.get(fullSymbol);
                    if (existingValue != null && existingValue instanceof PythonVariable) {
                        PythonVariable existingVariable = (PythonVariable)existingValue;
                        if (existingVariable.getValue() == null && assignedValue != null) {
                            existingVariable.setValue(parsedAssignedValue);
                        }

                    } else {
                        PythonPublicVariable targetVariable = codebase.findByFullName(fullSymbol, PythonPublicVariable.class);
                        if (targetVariable != null) {
                            PythonVariable newVariable = new PythonVariable(targetVariable.getName(), parsedAssignedValue);
                            newVariable.resolveSourceLocation(targetVariable);
                            workingMemory.put(fullSymbol, newVariable);
                        }
                    }
                }
            }
        });
    }

    private PythonValue findSymbol(String fullSymbolName) {
        if (workingMemory.containsKey(fullSymbolName)) {
            return workingMemory.get(fullSymbolName);
        } else if (parentContext != null) {
            return parentContext.findSymbol(fullSymbolName);
        } else {
            return null;
        }
    }
}
