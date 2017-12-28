package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class ExecutionContext {

    private PythonCodeCollection codebase;
    private Map<String, PythonValue> workingMemory = map();
    private PythonValue selfValue;
    private AbstractPythonStatement scope;
    private ExecutionContext parentContext = null;
    private int primaryScopeLevel = 0;

    public ExecutionContext(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue) {
        this.codebase = codebase;
        setSelfValue(selfValue);
    }

    public ExecutionContext(PythonCodeCollection codebase, PythonValue selfValue, AbstractPythonStatement scope) {
        this.codebase = codebase;
        setScope(scope);
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
        if (scope != null) {
            if (scope instanceof PythonFunction && scope.getChildStatements().size() > 0) {
                setPrimaryScopeLevel(scope.getChildStatements().get(0).getIndentationLevel());
            } else {
                setPrimaryScopeLevel(scope.getIndentationLevel());
            }
        }
    }

    public ExecutionContext getParentContext() {
        return parentContext;
    }

    public void setParentContext(ExecutionContext parentContext) {
        this.parentContext = parentContext;
        detectCircularChain();
    }

    public int getStackDepth() {
        int i = 0;
        ExecutionContext current = this;
        while (current != null) {
            i++;
            current = current.parentContext;
        }
        return i;
    }

    public void setPrimaryScopeLevel(int primaryScopeLevel) {
        this.primaryScopeLevel = primaryScopeLevel;
        if (this.primaryScopeLevel < 0) {
            this.primaryScopeLevel = 0;
        }
    }

    public int getPrimaryScopeLevel() {
        return primaryScopeLevel;
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

    /**
     * Resolves a PythonVariable to its contained value if its value is not null.
     * @param value
     * @return
     */
    public PythonValue resolveAbsoluteValue(PythonValue value) {
        return resolveValue(value, true);
    }


    /**
     * Resolves a PythonVariable to its contained value if its value is not null. This
     * will resolve to the inner-most PythonVariable if the final variable does not have
     * a value and has a source location.
     * @param value
     * @return
     */
    public PythonValue resolveValue(PythonValue value) {
        return resolveValue(value, false);
    }

    private PythonValue resolveValue(PythonValue value, boolean resolveAbsolute) {
        if (value instanceof PythonVariable) {
            PythonVariable asVariable = (PythonVariable)value;
            PythonValue result = asVariable.getValue();
            if (result == null) {
                if (workingMemory.containsKey(asVariable.getLocalName())) {
                    PythonValue existingVariable = workingMemory.get(asVariable.getLocalName());
                    if (existingVariable == value) {
                        return value;
                    } else {
                        return resolveValue(workingMemory.get(asVariable.getLocalName()), resolveAbsolute);
                    }
                } else if (asVariable.getSourceLocation() != null && resolveAbsolute) {
                    return value;
                } else {
                    return null;
                }
            } else {
                return resolveValue(result, resolveAbsolute);
            }
        } else {
            return value;
        }
    }



    public AbstractPythonStatement findSymbolDeclaration(String symbol) {
        if (symbol == null || codebase == null || scope == null) {
            return null;
        }

        AbstractPythonStatement result;
        if ((result = codebase.findByFullName(symbol)) != null) {
            return result;
        } else if ((result = codebase.resolveLocalSymbol(symbol, scope)) != null) {
            return result;
        } else {
            return null;
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
        while (currentContext != null) {
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
                valueMap.put(symbolName, resolveAbsoluteValue(newValue));
            } else {
                valueMap.put(symbolName, new PythonVariable(symbolName, resolveAbsoluteValue(newValue)));
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

    public void loadClassMembers() {
        final ExecutionContext ctx = this;
        codebase.traverse(new AbstractPythonVisitor() {
            @Override
            public void visitClass(PythonClass pyClass) {
                super.visitClass(pyClass);


            }
        });
    }

    private PythonValue findSymbol(String fullSymbolName) {
        if (fullSymbolName.endsWith("self") || fullSymbolName.endsWith(".self")) {
            return selfValue;
        }
        if (workingMemory.containsKey(fullSymbolName)) {
            return workingMemory.get(fullSymbolName);
        } else {
            PythonValue subPathValue = findSymbolAsSubPath(fullSymbolName);
            if (subPathValue != null) {
                return subPathValue;
            } else if (parentContext != null) {
                return parentContext.findSymbol(fullSymbolName);
            } else {
                return null;
            }
        }
    }

    private PythonValue findSymbolAsSubPath(String fullSymbolName) {
        String[] symbolPath = StringUtils.split(fullSymbolName, '.');

        PythonValue result = null;

        StringBuilder subPath = new StringBuilder();
        for (String path : symbolPath) {
            if (subPath.length() > 0) {
                subPath.append('.');
            }

            subPath.append(path);
            String workingPath = subPath.toString();
            if (workingMemory.containsKey(workingPath)) {
                result = workingMemory.get(workingPath);
            } else {
                break;
            }
        }

        String remainingPath = fullSymbolName.replace(subPath.toString(), "");

        if (remainingPath.length() > 0 && result != null && (result = resolveAbsoluteValue(result)) != null) {
            if (remainingPath.startsWith(".")) {
                remainingPath = remainingPath.substring(1);
            }

            //  Can only resolve member
            if (!(result instanceof PythonObject)) {

                int numMatched = StringUtils.countMatches(remainingPath, ".");
                for (int i = numMatched; i < symbolPath.length; i++) {
                    String path = symbolPath[i];

                }
            }
        }

        return result;
    }

    private void detectCircularChain() {
        List<ExecutionContext> detectedContexts = list();
        ExecutionContext current = this;
        while (current != null) {
            assert !detectedContexts.contains(current) : "Circular execution context chain detected!";
            detectedContexts.add(current);
            current = current.parentContext;
        }
    }
}
