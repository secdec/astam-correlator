package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Parses individual python code strings to generate a binary expression tree.
 */
public class PythonExpressionParser {

    private PythonCodeCollection linkedCodebase;
    private PythonValueBuilder valueBuilder = new PythonValueBuilder();
    private ExpressionDeconstructor expressionDeconstructor = new ExpressionDeconstructor();

    public PythonExpressionParser(PythonCodeCollection codebase) {
        this.linkedCodebase = codebase;
    }

    enum OperationType {
        UNKNOWN, INVALID,
        PRIMITIVE_OPERATION,
        MEMBER_ACCESS, TUPLE_REFERENCE, INDEXER, RETURN_STATEMENT,
        PARAMETER_ENTRY, // For tuples (a, b, c) and multi-assignment 'a, b = 1, 2'
        FUNCTION_CALL
    }

    public PythonExpression processString(String stringValue, AbstractPythonStatement context) {
        return processString(stringValue, context, null);
    }

    public PythonExpression processString(String stringValue, AbstractPythonStatement context, List<PythonValue> subjects) {

        PythonExpression result = null;

        List<String> expressions = expressionDeconstructor.deconstruct(stringValue);

        //  Ensure this is a supported expression
        for (String subexpr : expressions) {
            if (Language.PYTHON_KEYWORDS.contains(subexpr)) {
                return new IndeterminateExpression();
            }
        }

        OperationType operationType = OperationType.UNKNOWN;
        List<OperationType> expressionOperations = new ArrayList<OperationType>(expressions.size());
        String operationTypeIndicator = null;

        ScopeTracker scopeTracker = new ScopeTracker();

        int primaryEndIndex = 0;
        for (int i = 0; i < expressions.size(); i++) {
            String subexpr = expressions.get(i);
            for (int m = 0; m < subexpr.length(); m++) {
                scopeTracker.interpretToken(subexpr.charAt(m));
            }
            OperationType subexprOperation = detectOperationType(subexpr);

            if (subexprOperation != OperationType.INVALID && subexprOperation != OperationType.UNKNOWN && !scopeTracker.isInString() && !scopeTracker.isInScope()) {

                if (i > 0) {
                    OperationType lastType = expressionOperations.get(i - 1);
                    if (subexprOperation == OperationType.TUPLE_REFERENCE && lastType == OperationType.UNKNOWN || lastType == OperationType.MEMBER_ACCESS) {
                        // Any '(..)'-format expression is treated as a TUPLE_REFERENCE. A function call would
                        // have a symbol name detected as an UNKNOWN or MEMBER_ACCESS operation followed by a TUPLE_REFERENCE.
                        // ie 'someFunc(..)' -> 'someFunc', '(..)' -> 'UNKNOWN', 'TUPLE_REFERENCE'
                        subexprOperation = OperationType.FUNCTION_CALL;
                    }

                    //  An indexer expression 'arr[0]' will have an UNKNOWN token 'arr' followed by the INDEXER token '[0]'
                    if (subexprOperation == OperationType.INDEXER && lastType != OperationType.UNKNOWN) {
                        subexprOperation = OperationType.UNKNOWN;
                    }
                }

                if (operationType == OperationType.UNKNOWN &&
                        ((subexprOperation != OperationType.TUPLE_REFERENCE &&
                        subexprOperation != OperationType.MEMBER_ACCESS) || i == 0)) {
                    operationType = subexprOperation;
                    operationTypeIndicator = subexpr;
                    primaryEndIndex = i;
                }

                expressionOperations.add(subexprOperation);

            } else {
                expressionOperations.add(subexprOperation);
            }
        }

        //  If no high-level expressions have been detected it may be a partial expression, attempt to use that
        //  as the primary operation.

        if (operationType == OperationType.UNKNOWN) {
            for (int i = 0; i < expressionOperations.size(); i++) {
                OperationType type = expressionOperations.get(i);
                if (type != OperationType.UNKNOWN && type != OperationType.INVALID) {
                    operationType = type;
                    operationTypeIndicator = expressions.get(i);
                    break;
                }
            }
        }

        switch (operationType) {
            case PRIMITIVE_OPERATION:
                result = parsePrimitiveOperation(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex,
                        operationType,
                        operationTypeIndicator
                );
                break;
            case FUNCTION_CALL:
                result = parseFunctionCall(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex
                );
                break;
            case MEMBER_ACCESS:
                result = parseMemberAccess(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex
                );
                break;
            case INDEXER:
                result = parseIndexer(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex
                );
                break;
            case RETURN_STATEMENT:
                result = parseReturnStatement(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex
                );
                break;
            case TUPLE_REFERENCE:
                result = parseTupleReference(
                        expressions,
                        subjects,
                        expressionOperations,
                        context,
                        primaryEndIndex
                );
                break;
            default:
                result = null;
        }


        if (result == null) {
            return new IndeterminateExpression();
        } else {
            resolveSubValues(result, context);
            return result;
        }
    }

    OperationType detectOperationType(String expression) {
        if (expression.equals("return")) {
            return OperationType.RETURN_STATEMENT;
        } else if (expression.equals(".")) {
            return OperationType.MEMBER_ACCESS;
        } else if (PrimitiveOperationExpression.interpretOperator(expression) != PrimitiveOperationType.UNKNOWN) {
            return OperationType.PRIMITIVE_OPERATION;
        } else if (expression.startsWith("(")) {
            return OperationType.TUPLE_REFERENCE;
        } else if (expression.equals(",")) {
            return OperationType.PARAMETER_ENTRY;
        } else if (expression.startsWith("[")) {
            return OperationType.INDEXER;
        } else if (Language.PYTHON_KEYWORDS.contains(expression)) {
            return OperationType.INVALID;
        } else {
            return OperationType.UNKNOWN;
        }
    }

    PythonExpression parsePrimitiveOperation(List<String> expressions,
                                                   List<PythonValue> subjects,
                                                   List<OperationType> expressionTypes,
                                                   AbstractPythonStatement context,
                                                   int primaryEndIndex,
                                                   OperationType type,
                                                   String operationIndicator) {

        //  NOTE - Does not conform to PEMDAS order of operations! Operations are parsed left-to-right!

        PrimitiveOperationType primitiveType = PrimitiveOperationExpression.interpretOperator(operationIndicator);

        List<PythonValue> operands = null;
        PrimitiveOperationExpression result = new PrimitiveOperationExpression(primitiveType);

        /* Gather Operands */
        int nextOperationIdx = findNextOperation(expressionTypes, primaryEndIndex + 1);
        if (nextOperationIdx < 0) {
            nextOperationIdx = expressions.size() - 1;
        }
        OperationType nextOperation = expressionTypes.get(nextOperationIdx);
        //  If the next expression is a tuple and is also the last expression, parse that
        //  tuple directly and use it as our subject
        if (nextOperationIdx == expressions.size() - 1) {
            if (nextOperation == OperationType.TUPLE_REFERENCE) {
                String group = expressions.get(nextOperationIdx);
                PythonTuple pyGroup = valueBuilder.buildFromSymbol(group, PythonTuple.class);
                if (pyGroup != null) {
                    operands = new ArrayList<PythonValue>(pyGroup.getEntries());
                }
            } else {
                PythonValue operand;
                if (nextOperation == OperationType.FUNCTION_CALL) {
                    String functionCall = reconstructExpression(expressions, nextOperationIdx - 1, nextOperationIdx + 1);
                    operand = processString(functionCall, context);
                } else {
                    operand = tryMakeValue(expressions.get(nextOperationIdx), context, null);
                }
                operands = list((PythonValue)operand);
            }
        } else {
            String remainingExpression = reconstructExpression(expressions, primaryEndIndex + 1);
            PythonValue operand = tryMakeValue(remainingExpression, context, null);
            operands = list((PythonValue)operand);
        }

        if (subjects == null) {
            subjects = tryMakeSubjectValues(expressions, expressionTypes, primaryEndIndex - 1, context, list((PythonValue)result));
        }

        if (operands != null && subjects != null) {
            result.setOperands(operands);
            result.setSubjects(subjects);
            result = PrimitiveOperationExpression.rectifyOrderOfOperations(result);
            return result;
        } else {
            return new IndeterminateExpression();
        }
    }

    PythonExpression parseFunctionCall(List<String> expressions,
                                       List<PythonValue> subjects,
                                       List<OperationType> expressionTypes,
                                       AbstractPythonStatement context,
                                       int primaryEndIndex) {

        FunctionCallExpression callExpression = new FunctionCallExpression();

        /* Collect operands */
        List<PythonValue> operands = list();
        String primaryOperand = expressions.get(primaryEndIndex);
        String[] parameterEntries = gatherGroupEntries(primaryOperand);
        for (String entry : parameterEntries) {
            operands.add(tryMakeValue(entry, context, null));
        }

        /* Collect subjects */
        if (subjects == null) {
            subjects = tryMakeSubjectValues(expressions, expressionTypes, primaryEndIndex - 1, context, list((PythonValue)callExpression));
        }

        //  The function call may have trailing expressions, if so, then this function call is a subject of
        //  the following expressions
        if (subjects != null) {
            callExpression.setSubjects(subjects);
            callExpression.setParameters(operands);

            if (primaryEndIndex != expressions.size() - 1) {
                //  There are trailing expressions, generate it with this as the subject
                String remainingExpressions = reconstructExpression(expressions, primaryEndIndex + 1);
                PythonExpression trailingExpression = processString(remainingExpressions, context, list((PythonValue)callExpression));
                return trailingExpression;
            }

            return callExpression;
        } else {
            return new IndeterminateExpression();
        }
    }

    PythonExpression parseMemberAccess(List<String> expressions,
                                       List<PythonValue> subjects,
                                       List<OperationType> expressionTypes,
                                       AbstractPythonStatement context,
                                       int primaryEndIndex) {

        MemberExpression memberExpression = new MemberExpression();
        assert subjects != null : "No subjects were provided for a MemberExpression, but subjects are required for this expression!";
        memberExpression.setSubjects(subjects);

        int lastMemberAccessExpression = primaryEndIndex;
        OperationType currentExpressionType = OperationType.MEMBER_ACCESS;
        while (lastMemberAccessExpression + 1 < expressions.size() && (currentExpressionType == OperationType.MEMBER_ACCESS || currentExpressionType == OperationType.UNKNOWN)) {
            currentExpressionType = expressionTypes.get(++lastMemberAccessExpression);
        }

        if (currentExpressionType != OperationType.MEMBER_ACCESS && currentExpressionType != OperationType.UNKNOWN) {
            --lastMemberAccessExpression;
        }

        for (int i = 0; i <= lastMemberAccessExpression; i++) {
            if (expressionTypes.get(i) != OperationType.MEMBER_ACCESS) {
                memberExpression.appendPath(expressions.get(i));
            }
        }

        if (lastMemberAccessExpression != expressionTypes.size() - 1) {
            String remainingExpression = reconstructExpression(expressions, lastMemberAccessExpression);
            PythonExpression trailingExpression = processString(remainingExpression, context, list((PythonValue)memberExpression));
            return trailingExpression;
        }

        return memberExpression;
    }

    PythonExpression parseIndexer(List<String> expressions,
                                       List<PythonValue> subjects,
                                       List<OperationType> expressionTypes,
                                       AbstractPythonStatement context,
                                       int primaryEndIndex) {

        IndexerExpression indexerExpression = new IndexerExpression();

        String indexerText = expressions.get(primaryEndIndex);
        indexerText = CodeParseUtil.trim(indexerText, new String[] { "[", "]" }, 1);
        List<String> indexerSubExpressions = expressionDeconstructor.deconstruct(indexerText);

        if (indexerSubExpressions.size() <= 1) {
            PythonValue indexerValue = tryMakeValue(indexerText, context, null);
            indexerExpression.setIndexerValue(indexerValue);
        } else {
            PythonValue indexerValue = processString(indexerText, context);
            indexerExpression.setIndexerValue(indexerValue);
        }

        if (subjects == null) {
            subjects = tryMakeSubjectValues(expressions, expressionTypes, primaryEndIndex - 1, context, null);
            if (subjects != null) {
                indexerExpression.setSubjects(subjects);
            }
        }

        if (primaryEndIndex != expressions.size() - 1) {
            String remainingExpressionText = reconstructExpression(expressions, primaryEndIndex + 1);
            PythonExpression remainingExpression = processString(remainingExpressionText, context, list((PythonValue)indexerExpression));
            return remainingExpression;
        }

        return indexerExpression;
    }

    PythonExpression parseReturnStatement(List<String> expressions,
                                  List<PythonValue> subjects,
                                  List<OperationType> expressionTypes,
                                  AbstractPythonStatement context,
                                  int primaryEndIndex) {

        ReturnExpression returnExpression = new ReturnExpression();

        if (primaryEndIndex != expressions.size() - 1) {
            String subjectExpression = reconstructExpression(expressions, primaryEndIndex + 1);
            PythonValue value = tryMakeValue(subjectExpression, context, null);
            returnExpression.addSubject(value);
        }

        return returnExpression;
    }

    PythonExpression parseTupleReference(List<String> expressions,
                                          List<PythonValue> subjects,
                                          List<OperationType> expressionTypes,
                                          AbstractPythonStatement context,
                                          int primaryEndIndex) {

        //  A direct tuple reference will occur if parentheses are used to order operations. All
        //  other cases are implemented as subsets of the other expression types.

        //  The tuple reference needs to be the first entry in the expressions. Any preceding
        //  expressions must be parsed before the tuple reference is parsed.
        if (primaryEndIndex != 0) {
            return null;
        }

        ScopingExpression result = new ScopingExpression();

        String tupleString = expressions.get(primaryEndIndex);
        tupleString = CodeParseUtil.trim(tupleString, new String[] { "(", ")" }, 1);
        PythonValue tupleExpression = tryMakeValue(tupleString, context, null);
        result.addSubject(tupleExpression);

        if (expressions.size() > 1) {
            String remainingString = reconstructExpression(expressions, primaryEndIndex + 1);
            PythonValue remainingExpression = tryMakeValue(remainingString, context, list((PythonValue)result));
            if (remainingExpression instanceof PythonExpression) {
                return (PythonExpression)remainingExpression;
            } else {
                //  A trailing expression was added that could not be parsed as an expression - this makes
                //  no sense as a value would directly follow another value, which is invalid syntax.
                return null;
            }
        } else {
            return result;
        }
    }

    String[] gatherGroupEntries(String groupExpression) {
        groupExpression = CodeParseUtil.trim(groupExpression, new String[] { "[", "]", "{", "}", "(", ")" }, 1);
        return CodeParseUtil.splitByComma(groupExpression);
    }

    int findNextOperation(List<OperationType> operations, int startIndex) {
        for (int i = startIndex; i < operations.size(); i++) {
            OperationType current = operations.get(i);
            if (current != OperationType.UNKNOWN) {
                return i;
            }
        }
        return -1;
    }

    String reconstructExpression(List<String> expressionParts, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder();

        String lastPart = null;
        for (int i = startIndex; i < expressionParts.size() && i < endIndex; i++) {
            String part = expressionParts.get(i);
            if (i > startIndex && lastPart.equals("return")) {
                sb.append(' ');
            }
            sb.append(part);
            lastPart = part;
        }

        return sb.toString();
    }

    String reconstructExpression(List<String> expressionParts, int startIndex) {
        return reconstructExpression(expressionParts, startIndex, Integer.MAX_VALUE);
    }

    PythonValue tryMakeValue(String expression, AbstractPythonStatement context, List<PythonValue> expressionSubject) {
        PythonValue asValue = valueBuilder.buildFromSymbol(expression);
        if (isValidValue(asValue)) {
            return asValue;
        } else {
            return processString(expression, context, expressionSubject);
        }
    }

    List<PythonValue> tryMakeSubjectValues(List<String> expressions,
                                           List<OperationType> expressionTypes,
                                           int endIndex,
                                           AbstractPythonStatement context,
                                           List<PythonValue> expressionSubject) {

        OperationType subjectType = OperationType.UNKNOWN;
        for (int i = 0; i < endIndex; i++) {
            OperationType exprType = expressionTypes.get(i);
            if (exprType == OperationType.PARAMETER_ENTRY ||
                    exprType == OperationType.TUPLE_REFERENCE ||
                    exprType == OperationType.MEMBER_ACCESS) {
                subjectType = exprType;
                break;
            }
        }

        List<PythonValue> subjects = null;

        switch (subjectType) {
            case PARAMETER_ENTRY:
            case TUPLE_REFERENCE:
                String[] entries = gatherGroupEntries(reconstructExpression(expressions, 0, endIndex + 1));
                if (entries != null) {
                    subjects = list();
                    for (String entry : entries) {
                        subjects.add(tryMakeValue(entry, context, expressionSubject));
                    }
                }
                break;

            case MEMBER_ACCESS:
                String memberPath = reconstructExpression(expressions, 0, endIndex + 1);
                PythonValue value = new PythonObject(memberPath);
                subjects = list(value);
                break;

            default:
                String subjectExpression = reconstructExpression(expressions, 0, endIndex + 1);
                PythonValue asValue = valueBuilder.buildFromSymbol(subjectExpression);
                if (isValidValue(asValue)) {
                    subjects = list(asValue);
                } else {
                    PythonExpression asExpression = processString(subjectExpression, context, expressionSubject);
                    subjects = list((PythonValue)asExpression);
                }
                break;
        }

        return subjects;
    }

    void resolveSubValues(PythonValue value, AbstractPythonStatement context) {
        List<PythonValue> subValues = value.getSubValues();
        if (subValues == null) {
            return;
        } else {
            subValues = new LinkedList<PythonValue>(value.getSubValues());
        }
        while (subValues.size() > 0) {
            PythonValue subValue = subValues.get(0);
            if (subValue instanceof PythonUnresolvedValue) {
                PythonUnresolvedValue unresolvedValue = (PythonUnresolvedValue)subValue;
                PythonValue resolvedValue = tryMakeValue(unresolvedValue.getStringValue(), context, null);
                if (!(resolvedValue instanceof PythonUnresolvedValue)) {
                    value.resolveSubValue(subValue, resolvedValue);
                    subValue = resolvedValue;
                }
            }

            resolveSubValues(subValue, context);
            subValues.remove(0);
        }
    }

    boolean isValidValue(PythonValue value) {
        return value != null && !(value instanceof PythonIndeterminateValue);
    }

    boolean isValidExpression(PythonExpression expression) {
        return expression != null && !(expression instanceof IndeterminateExpression);
    }
}
