package com.denimgroup.threadfix.framework.impl.django.python.pythonInstructions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class Conditional extends AbstractPythonInstruction {

    String condition;

    Collection<AbstractPythonInstruction> currentBranchInstructions = list();
    List<Collection<AbstractPythonInstruction>> subInstructions = list();

    public Conditional(String condition) {
        this.condition = condition;
        this.subInstructions.add(currentBranchInstructions);
    }

    public void addSiblingConditional() {
        currentBranchInstructions = list();
        subInstructions.add(currentBranchInstructions);
    }

    //  Evaluate is not applicable to this instruction
    @Override
    public Collection<String> evaluate() {
        return null;
    }

    public void addChildInstruction(AbstractPythonInstruction instruction) {
        currentBranchInstructions.add(instruction);
    }

    public int getNumBranches() {
        return subInstructions.size();
    }

    public Collection<AbstractPythonInstruction> getBranchInstructions(int branchIndex) {
        return subInstructions.get(branchIndex);
    }
}
