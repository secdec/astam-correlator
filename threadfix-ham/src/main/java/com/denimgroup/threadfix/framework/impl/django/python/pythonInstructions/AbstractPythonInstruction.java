package com.denimgroup.threadfix.framework.impl.django.python.pythonInstructions;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class AbstractPythonInstruction {

    List<AbstractPythonInstruction> dependencies = list();
    List<AbstractPythonInstruction> childInstructions = list();

    //  A single instruction may have multiple evaluated results in the case of conditionals
    public abstract Collection<String> evaluate();

    public Collection<AbstractPythonInstruction> getDependencies() {
        return dependencies;
    }

    public Collection<AbstractPythonInstruction> getChildInstructions() {
        return childInstructions;
    }

    public void addDependency(AbstractPythonInstruction instruction) {
        dependencies.add(instruction);
    }

    public void addDependencies(Collection<AbstractPythonInstruction> instructions) {
        dependencies.addAll(instructions);
    }

    public void addChildInstruction(AbstractPythonInstruction instruction) {
        childInstructions.add(instruction);
    }

}
