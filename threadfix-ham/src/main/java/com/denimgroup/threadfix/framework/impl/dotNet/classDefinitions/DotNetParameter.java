package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DotNetParameter {
    private int parameterIndex;
    private String name;
    private String defaultValue;
    private String value;
    private String type;
    private boolean isExtensionParameter;
    private List<DotNetAttribute> attributes = list();

    public boolean isDeclaration() {
        return value == null;
    }

    public boolean isValue() {
        return value != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isExtensionParameter() {
        return isExtensionParameter;
    }

    public void setIsExtensionParameter(boolean extensionParameter) {
        isExtensionParameter = extensionParameter;
    }

    public List<DotNetAttribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(DotNetAttribute attribute) {
        attributes.add(attribute);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!attributes.isEmpty()) {
            boolean isFirst = true;
            for (DotNetAttribute attribute : attributes) {
                if (!isFirst) {
                    sb.append(' ');
                }
                sb.append(attribute.toString());
                isFirst = false;
            }
            sb.append(' ');
        }

        if (isExtensionParameter) {
            sb.append("this ");
        }

        if (isDeclaration()) {
            if (type != null && type.isEmpty()) {
                sb.append("{EmptyType}");
            } else {
                sb.append(type);
            }
            sb.append(' ');
            if (name != null && name.isEmpty()) {
                sb.append("{EmptyName}");
            } else {
                sb.append(name);
            }
            if (defaultValue != null) {
                sb.append(" = ");

                if (defaultValue.isEmpty()) {
                    sb.append("{EmptyDefaultValue}");
                } else {
                    sb.append(defaultValue);
                }
            }
        } else {
            if (value.isEmpty()) {
                sb.append("{EmptyValue}");
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }
}
