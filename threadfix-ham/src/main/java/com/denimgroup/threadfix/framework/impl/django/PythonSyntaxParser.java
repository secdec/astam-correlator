package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonSyntaxParser implements EventBasedTokenizer {

    public static PythonCodeCollection run(File rootDirectory) {
        PythonCodeCollection codebase = new PythonCodeCollection();
        Collection<File> allFiles = FileUtils.listFiles(rootDirectory, new String[] { "py" }, true);
        for (File file : allFiles) {

            if (FilePathUtils.getRelativePath(file, rootDirectory).contains("build")) {
                continue;
            }

            PythonSyntaxParser parser = new PythonSyntaxParser();
            EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);

            Collection<String> classNames = parser.getClassNames();
            Collection<String> functionNames = parser.getFunctionNames();

            if (classNames.size() > 0) {
                codebase.addClasses(file.getAbsolutePath(), classNames);
            }

            if (functionNames.size() > 0) {
                codebase.addFunctions(file.getAbsolutePath(), functionNames);
            }
        }
        return codebase;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    boolean nextIsClassName = false;
    boolean nextIsFunctionName = false;
    List<String> classNames = list();
    List<String> functionNames = list();

    public Collection<String> getClassNames() {
        return classNames;
    }

    public Collection<String> getFunctionNames() {
        return functionNames;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (nextIsClassName) {
                classNames.add(stringValue);
                nextIsClassName = false;
            } else if (nextIsFunctionName) {
                functionNames.add(stringValue);
                nextIsFunctionName = false;
            }

            if (stringValue.equalsIgnoreCase("class")) {
                nextIsClassName = true;
            } else if (stringValue.equalsIgnoreCase("def")) {
                nextIsFunctionName = true;
            }
        }
    }
}
