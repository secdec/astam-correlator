package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DotNetAttribute {
    private String name;
    private List<DotNetParameter> parameters = list();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DotNetParameter> getParameters() {
        return parameters;
    }

    public void addParameter(DotNetParameter parameter) {
        parameters.add(parameter);
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
            for (DotNetParameter param : parameters) {
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
