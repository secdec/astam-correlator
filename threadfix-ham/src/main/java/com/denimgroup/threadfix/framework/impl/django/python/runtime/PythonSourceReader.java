package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CondensedLineEntry;
import com.denimgroup.threadfix.framework.util.CondensedLinesMap;
import com.denimgroup.threadfix.framework.util.FileReadUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonSourceReader {

    private File target = null;
    private CondensedLinesMap linesMap;
    private boolean allowedLines[];

    public PythonSourceReader(@Nonnull File file) {
        this(file, false);
    }

    public PythonSourceReader(@Nonnull  File file, boolean acceptAllLines) {
        target = file;

        linesMap = FileReadUtils.readLinesCondensed(file.getAbsolutePath(), -1, -1);
        int numLines = linesMap.getLineEntries().size();
        allowedLines = new boolean[numLines];
        for (int i = 0; i < numLines; i++) {
            allowedLines[i] = acceptAllLines;
        }
    }

    public List<String> getLines() {
        List<String> lines = list();
        List<String> condensedLines = linesMap.getCondensedLines();
        for (int i = 0; i < condensedLines.size(); i++) {
            Collection<CondensedLineEntry> lineEntries = linesMap.getEntriesForCondensedLine(i);
            boolean acceptLine = true;
            for (CondensedLineEntry line : lineEntries) {
                if (!allowedLines[line.sourceLineNumber]) {
                    acceptLine = false;
                    break;
                }
            }
            if (acceptLine) {
                lines.add(condensedLines.get(i));
            }
        }
        return lines;
    }

    public void ignore(int startLine, int endLine) {
        for (int i = startLine; i < endLine && i < allowedLines.length; i++) {
            allowedLines[i - 1] = false;
        }
    }

    public void accept(int startLine, int endLine) {
        for (int i = startLine; i < endLine && i < allowedLines.length; i++) {
            allowedLines[i - 1] = true;
        }
    }

    public void ignore(@Nonnull AbstractPythonStatement statement) {
        verifyStatement(statement);
        ignore(statement.getSourceCodeStartLine(), statement.getSourceCodeEndLine());
    }

    public void accept(@Nonnull AbstractPythonStatement statement) {
        verifyStatement(statement);
        accept(statement.getSourceCodeStartLine(), statement.getSourceCodeEndLine());
    }

    /**
     * Accepts lines belonging to children of the given statement that match the given types.
     */
    public void acceptChildren(@Nonnull AbstractPythonStatement statement, Class<?> ...types) {
        for (AbstractPythonStatement child : statement.getChildStatements()) {
            if (!isAssignableFrom(child.getClass(), types)) {
                continue;
            }

            accept(child);
        }
    }

    /**
     * Ignore lines belonging to children of the given statement that match the given types.
     */
    public void ignoreChildren(@Nonnull AbstractPythonStatement statement, Class<?> ...types) {
        for (AbstractPythonStatement child : statement.getChildStatements()) {
            if (!isAssignableFrom(child.getClass(), types)) {
                continue;
            }

            ignore(child);
        }
    }



    /**
     * Accepts lines belonging to the given statement while denying its children after the given depth.
     */
    public void accept(@Nonnull AbstractPythonStatement statement, int maxDepth) {
        predicate(statement, maxDepth, true);
    }

    /**
     * Accepts lines belonging to the given statement while denying its children after the given depth.
     * @param types Only python statements of the given types will be accepted.
     */
    public void accept(@Nonnull AbstractPythonStatement statement, int maxDepth, Class<AbstractPythonStatement> ...types) {
        predicate(statement, maxDepth, true, types);
    }


    /**
     * Ignores lines belonging to the given statement while accepting its children after the given depth.
     */
    public void ignore(@Nonnull AbstractPythonStatement statement, int maxDepth) {
        predicate(statement, maxDepth, false);
    }

    /**
     * Ignore lines belonging to the given statement while accepting its children after the given depth.
     * @param types Only python statements of the given types will be ignored.
     */
    public void ignore(@Nonnull AbstractPythonStatement statement, int maxDepth, Class<AbstractPythonStatement> ...types) {
        predicate(statement, maxDepth, false, types);
    }





    private void predicate(@Nonnull AbstractPythonStatement statement, int maxDepth, boolean shouldAllow, Class<AbstractPythonStatement> ...types) {
        verifyStatement(statement);
        accept(statement);
        List<AbstractPythonStatement> children = statement.getChildStatements();
        if (maxDepth <= 0) {
            for (AbstractPythonStatement child : children) {
                ignore(child);
            }
        } else {
            for (AbstractPythonStatement child : children) {
                Class childType = child.getClass();
                if (isAssignableFrom(childType, types)) {
                    accept(child, maxDepth - 1);
                } else {
                    ignore(child);
                }
            }
        }
    }

    private void predicate(@Nonnull AbstractPythonStatement statement, int maxDepth, boolean shouldAllow) {
        verifyStatement(statement);
        accept(statement);

        List<AbstractPythonStatement> children = statement.getChildStatements();
        if (maxDepth <= 0) {
            for (AbstractPythonStatement child : children) {
                ignore(child);
            }
        } else {
            for (AbstractPythonStatement child : children) {
                accept(child, maxDepth - 1);
            }
        }
    }

    private void verifyStatement(@Nonnull AbstractPythonStatement statement) {
        int startLine = statement.getSourceCodeStartLine();
        int endLine = statement.getSourceCodeEndLine();

        assert startLine >= 0 : "startLine must be non-negative in statement " + statement.toString();
        assert endLine >= 0 : "endLine must be non-negative in statement " + statement.toString();
    }

    private boolean isAssignableFrom(@Nonnull Class<?> checkedClass, @Nonnull Class<?>[] possibleClasses) {
        for (Class<?> type : possibleClasses) {
            if (type.isAssignableFrom(checkedClass)) {
                return true;
            }
        }
        return false;
    }

}
