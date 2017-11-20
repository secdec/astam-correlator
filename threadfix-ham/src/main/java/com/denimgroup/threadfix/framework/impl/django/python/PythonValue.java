package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonValue {
    PythonPublicVariable variable;
    PythonVariableDataType dataType;
    String sourceDataString;
    Object dataValue;

    public PythonValue(PythonPublicVariable variable) {
        this.variable = variable;
    }

    public PythonPublicVariable getVariable() {
        return variable;
    }

    public void setVariable(PythonPublicVariable variable) {
        this.variable = variable;
    }

    public PythonVariableDataType getDataType() {
        return dataType;
    }

    public void setDataType(PythonVariableDataType dataType) {
        this.dataType = dataType;
    }

    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

    public void setDataValue(Object dataValue, PythonVariableDataType dataType) {
        this.dataValue = dataValue;
        this.dataType = dataType;
    }

    public boolean isResolved() {
        return this.dataValue != null;
    }

    public boolean tryResolve(@Nonnull PythonCodeCollection codebase) {
        assert sourceDataString != null: "Cannot resolve PythonValue when no string data was provided";
        assert variable != null: "Cannot resolve PythonValue when no scoping node was provided";

        if (sourceDataString.startsWith("[")) {
            dataType = PythonVariableDataType.ARRAY;
            String formattedValue = this.sourceDataString;
            formattedValue = formattedValue.substring(1, formattedValue.length() - 1);
            String[] elements = CodeParseUtil.splitByComma(formattedValue);
            Collection<PythonValue> values = list();
            for (String el : elements) {
                PythonValue varValue = new PythonValue(this.variable);
                varValue.setSourceString(el);
                varValue.tryResolve(codebase);
            }
            dataValue = values;
            return true;
        } else if (sourceDataString.startsWith("\"") || sourceDataString.startsWith("'")
                ||sourceDataString.startsWith("r\"") || sourceDataString.startsWith("r'")) {
            dataType = PythonVariableDataType.STRING_LITERAL;
            String formattedString = sourceDataString;
            if (formattedString.startsWith("r")) {
                formattedString = formattedString.substring(1);
            }
            if (formattedString.startsWith("\"") || formattedString.startsWith("'")) {
                formattedString = formattedString.substring(1);
            }
            if (formattedString.endsWith("\"") || formattedString.endsWith("'")) {
                formattedString = formattedString.substring(0, formattedString.length() - 1);
            }

            dataValue = formattedString;
            return true;
        } else {
            //  Non-primitive, may be function call, variable, or new object
            String symbol = null;
            if (sourceDataString.contains("(")) {
                symbol = sourceDataString.substring(0, sourceDataString.indexOf('('));
            } else {
                symbol = sourceDataString;
            }

            AbstractPythonScope resolvedItem;
            resolvedItem = codebase.findByPartialName(this.variable, symbol);
            if (resolvedItem == null) {
                resolvedItem = codebase.findByFullName(symbol);
            }

            if (resolvedItem != null) {
                if (resolvedItem instanceof PythonPublicVariable) {

                } else if (resolvedItem instanceof PythonClass) {

                } else if (resolvedItem instanceof PythonFunction) {

                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return false;
    }

    public String getSourceString() {
        return sourceDataString;
    }

    public void setSourceString(String stringValue) {
        this.sourceDataString = stringValue.trim();
    }
}
