package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpAttribute {
    private String name;
    private List<CSharpParameter> parameters = list();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CSharpParameter> getParameters() {
        return parameters;
    }

    public void addParameter(CSharpParameter parameter) {
        parameters.add(parameter);
    }

    public CSharpParameter getParameterValue(String parameterName) {
        for (CSharpParameter parameter : parameters) {
            if (parameter.isValue() && parameterName.equals(parameter.getName())) {
                return parameter;
            }
        }

        return null;
    }

    public CSharpParameter getParameterValue(int parameterIndex) {
        if (parameters.size() > parameterIndex) {
            return parameters.get(parameterIndex);
        } else {
            return null;
        }
    }

    public CSharpParameter getParameterValue(String parameterName, int parameterIndex) {
        CSharpParameter result = getParameterValue(parameterName);
        if (result == null) {
            result = getParameterValue(parameterIndex);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (name != null && name.isEmpty()) {
            sb.append("{Unnammed}");
        } else {
            sb.append(name);
        }

        if (!parameters.isEmpty()) {
            sb.append('(');

            boolean isFirst = true;
            for (CSharpParameter param : parameters) {
                if (!isFirst)
                    sb.append(", ");

                sb.append(param.toString());
                isFirst = false;
            }

            sb.append(')');
        }

        sb.append(']');
        return sb.toString();
    }
}
