////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpParameter extends CanHaveAttributes {
    private int parameterIndex;
    private String name;
    private String defaultValue;
    private String value;
    private String type;
    private boolean isExtensionParameter;
    private boolean isNullable;

    private static List<String> NON_NULL_TYPES = list(
        "int", "long", "short", "char",
        "DateTime", "TimeSpan", "float", "double",
        "bool", "byte", "decimal"
    );

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

    public String getStringValue() {
        String result = value;
        if (result != null) {
            result = CodeParseUtil.trim(result, "\"");
        }
        return result;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

        if (type.contains("?")) {
            this.type = type.replaceAll("\\?", "");
            isNullable = true;
        } else {
            isNullable = NON_NULL_TYPES.contains(type);
        }
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isExtensionParameter() {
        return isExtensionParameter;
    }

    public void setIsExtensionParameter(boolean extensionParameter) {
        isExtensionParameter = extensionParameter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<CSharpAttribute> attributes = getAttributes();
        if (!attributes.isEmpty()) {
            boolean isFirst = true;
            for (CSharpAttribute attribute : attributes) {
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
            if (name != null) {
                sb.append(name);
                sb.append(" = ");
            }
            if (value.isEmpty()) {
                sb.append("{EmptyValue}");
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }
}
