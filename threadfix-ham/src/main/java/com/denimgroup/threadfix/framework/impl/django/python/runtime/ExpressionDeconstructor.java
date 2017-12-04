package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.util.ScopeTracker;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ExpressionDeconstructor {

    static final List<Character> SPECIAL_CHARS = list('%', '+', '-', '/', '*', '=', '.', '(', ')', '[', ']', '{', '}', ':', ',');

    public List<String> deconstruct(String fullExpression) {
        List<String> expressions = list();

        //   Takes an expression ie 5 + 5 == 10 and splits it into sub-expressions '5', '+', '5', '==', '10'

        boolean isSpecialExpression = false;
        StringBuilder workingSubExpression = new StringBuilder();

        ScopeTracker scopeTracker = new ScopeTracker();
        for (int i = 0; i < fullExpression.length(); i++) {
            char c = fullExpression.charAt(i);
            scopeTracker.interpretToken(c);

            if (!scopeTracker.isInString() && !scopeTracker.isInScope() && (!scopeTracker.enteredGlobalScope() || isSpecialExpression)) {
                if (SPECIAL_CHARS.contains(c)) {
                    if (workingSubExpression.length() > 0 && !isSpecialExpression) {
                        expressions.add(workingSubExpression.toString());
                        workingSubExpression = new StringBuilder();
                        workingSubExpression.append(c);
                    } else if (workingSubExpression.length() == 0 || isSpecialExpression) {
                        workingSubExpression.append(c);
                    }
                    isSpecialExpression = true;
                } else if (c == ' ') {
                    if (workingSubExpression.length() > 0) {
                        expressions.add(workingSubExpression.toString());
                        workingSubExpression = new StringBuilder();
                        isSpecialExpression = false;
                    }
                } else {
                    if (isSpecialExpression) {
                        if (workingSubExpression.length() > 0) {
                            expressions.add(workingSubExpression.toString());
                            workingSubExpression = new StringBuilder();
                        }
                        isSpecialExpression = false;
                    }

                    workingSubExpression.append(c);
                }
            } else {
                workingSubExpression.append(c);
            }
        }

        if (workingSubExpression.length() > 0) {
            expressions.add(workingSubExpression.toString());
        }

        return expressions;
    }

}
