package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class CSharpMethod extends CanHaveAttributes {
    private String name;
    private String returnType;
    private List<CSharpParameter> parameters = list();
    private int startLine, endLine;
    private boolean isStatic = false;
    private AccessLevel accessLevel = AccessLevel.PRIVATE;

    public enum AccessLevel {
        PUBLIC,
        PROTECTED,
        PRIVATE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<CSharpParameter> getParameters() {
        return parameters;
    }

    public void addParameter(CSharpParameter parameter) {
        parameters.add(parameter);
    }

    public CSharpParameter getParameter(int index) {
        return parameters.get(index);
    }

    public CSharpParameter getParameter(String parameterName) {
        for (CSharpParameter param : parameters) {
            if (param.getName().equals(parameterName)) {
                return param;
            }
        }
        return null;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isExtensionMethod() {
        if (!isStatic || parameters.isEmpty()) {
            return false;
        }

        return parameters.get(0).isExtensionParameter();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('(');
        result.append(startLine);
        result.append(':');
        result.append(endLine);
        result.append(") ");

        result.append(accessLevel);
        result.append(' ');
        if (isStatic)
            result.append("static ");

        if (returnType == null) {
            result.append("{NullReturnType}");
        } else if (returnType.isEmpty()) {
            result.append("{EmptyReturnType}");
        } else {
            result.append(returnType);
        }
        result.append(' ');
        if (name == null) {
            result.append("{NullName}");
        } else if (name.isEmpty()) {
            result.append("{EmptyName}");
        } else {
            result.append(name);
        }
        result.append('(');

        boolean isFirst = true;
        for (CSharpParameter param : parameters) {
            if (!isFirst) {
                result.append(", ");
            }
            result.append(param.toString());
            isFirst = false;
        }

        result.append(") (");
        result.append(getAttributes().size());
        result.append(" attributes)");

        return result.toString();
    }
}
