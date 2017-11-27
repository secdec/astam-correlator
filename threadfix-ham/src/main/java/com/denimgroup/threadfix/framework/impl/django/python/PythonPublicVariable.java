package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.util.Map;

public class PythonPublicVariable extends AbstractPythonStatement {

    String name;
    String valueString;
    PythonClass resolvedTypeClass;
    boolean isArray = false;

    public void setResolvedTypeClass(PythonClass pyClass) {
        resolvedTypeClass = pyClass;
    }

    public PythonClass getResolvedTypeClass() {
        return resolvedTypeClass;
    }

    @Override
    public void addImport(String importedItem, String alias) {

    }

    @Override
    public Map<String, String> getImports() {
        return this.findParent(PythonModule.class).getImports();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonPublicVariable clone = new PythonPublicVariable();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.valueString = this.valueString;
        clone.resolvedTypeClass = this.resolvedTypeClass;
        return clone;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;

        isArray = valueString.startsWith("[");

        this.clearChildStatements();

        if (isArray) {
            valueString = valueString.substring(1, valueString.length() - 1);
            String[] values = CodeParseUtil.splitByComma(valueString);
            int i = 0;
            for (String value : values) {
                int idx = i++;

                PythonPublicVariable element = new PythonPublicVariable();
                element.setSourceCodeLine(this.getSourceCodeLine());
                element.setSourceCodePath(this.getSourceCodePath());
                element.setName("[" + idx + "]");
                element.setValueString(value);

                this.addChildStatement(element);

                PythonVariableModification assignment = new PythonVariableModification();
                assignment.setTarget(element.getFullName());
                assignment.setSourceCodeLine(this.getSourceCodeLine());
                assignment.setSourceCodePath(this.getSourceCodePath());
                assignment.setName("Array[" + idx + "] = " + value);
                assignment.setOperatorValue(value);
                assignment.setModificationType(VariableModificationType.ASSIGNMENT);

                this.addChildStatement(assignment);
            }
        }
    }

    public String getValueString() {
        return valueString;
    }
}
