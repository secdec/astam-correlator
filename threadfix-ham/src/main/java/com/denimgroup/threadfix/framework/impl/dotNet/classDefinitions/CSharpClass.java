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

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpClass extends CanHaveAttributes {
    private String name;
    private String namespace;
    private List<String> templateParameterNames = list();
    private List<String> baseTypes = list();
    private boolean isStatic = false;
    private List<CSharpMethod> methods = list();
    private boolean isAbstract = false;

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getTemplateParameterNames() {
        return templateParameterNames;
    }

    public void addTemplateParameterName(String templateParamName) {
        templateParameterNames.add(templateParamName);
    }

    public List<String> getBaseTypes() {
        return baseTypes;
    }

    public void addBaseType(String baseType) {
        baseTypes.add(baseType);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public List<CSharpMethod> getMethods() {
        return methods;
    }

    public List<CSharpMethod> getMethods(CSharpMethod.AccessLevel accessLevel) {
        List<CSharpMethod> result = list();
        for (CSharpMethod method : methods) {
            if (method.getAccessLevel() == accessLevel) {
                result.add(method);
            }
        }
        return result;
    }

    public boolean hasMethod(CSharpMethod other) {
        for (CSharpMethod method : methods) {
            if (!method.getName().equals(other.getName())) {
                continue;
            }
            if (method.getParameters().size() != other.getParameters().size()) {
                continue;
            }
            if (!method.getReturnType().equals(other.getReturnType())) {
                continue;
            }
            boolean matches = true;
            for (CSharpParameter param : method.getParameters()) {
                CSharpParameter otherParam = other.getParameter(param.getName());
                if (otherParam == null) {
                    matches = false;
                    break;
                }
                if (!otherParam.getType().equals(param.getType())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }

        return false;
    }

    public void addMethod(CSharpMethod method) {
        methods.add(method);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isStatic) {
            sb.append("static ");
        }
        sb.append("class ");

        if (name == null) {
            sb.append("{NullName}");
        } else if (name.isEmpty()) {
            sb.append("{Unnammed}");
        } else {
            sb.append(name);
        }

        if (!templateParameterNames.isEmpty()) {
            sb.append('<');
            boolean isFirst = true;
            for (String paramName : templateParameterNames) {
                if (!isFirst) {
                    sb.append(", ");
                }
                if (paramName == null) {
                    sb.append("{NullName}");
                } else if (paramName.isEmpty()) {
                    sb.append("{EmptyName}");
                } else {
                    sb.append(paramName);
                }
                isFirst = false;
            }
            sb.append('>');
        }

        if (!baseTypes.isEmpty()) {
            sb.append(" : ");
            boolean isFirst = true;
            for (String baseType : baseTypes) {
                if (!isFirst) {
                    sb.append(", ");
                }
                if (baseType == null) {
                    sb.append("{NullBaseType}");
                } else if (baseType.isEmpty()) {
                    sb.append("{EmptyBaseType}");
                } else {
                    sb.append(baseType);
                }
                isFirst = false;
            }
        }

        sb.append(" (");
        sb.append(methods.size());
        sb.append(" methods, ");
        sb.append(getAttributes().size());
        sb.append(" attributes)");
        return sb.toString();
    }
}
