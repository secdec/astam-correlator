package com.denimgroup.threadfix.framework.impl.struts;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

// Used to detect the set of Struts project folders contained within a given directory
public class StrutsProjectDetector {
    public List<String> findProjectPaths(File basePath) {

        /* Search for struts-compatible XML files, and traverse parent directories from there until the parent directory
         *  contains java files. That parent directory will be a project root.
         */

        List<String> result = list();
        Collection<File> xmlFiles = FileUtils.listFiles(basePath, new String[] { "xml" }, true);
        for (File xmlFile : xmlFiles) {
            String fileContents;
            try {
                fileContents = FileUtils.readFileToString(xmlFile);
            } catch (IOException e) {
                fileContents = null;
            }

            if (fileContents == null) {
                continue;
            }

            if (fileContents.toLowerCase().contains("<struts>")) {
                String possibleProjectRoot = searchForParentWithStrutsNamespace(xmlFile, basePath);
                if (possibleProjectRoot != null && !result.contains(possibleProjectRoot)) {
                    result.add(possibleProjectRoot);
                }
            }
        }

        if (result.size() == 0) {
            result.add(basePath.getAbsolutePath());
        }

        //  Remove any top-level project roots (ie /foo/bar and /foo are project roots; remove /foo)
        List<String> filteredResult = list();
        for (String dir : result) {
            boolean skip = false;
            for (String other : result) {
                if (!other.equals(dir) && other.startsWith(dir + File.separator)) {
                    skip = true;
                    break;
                }
            }

            if (!skip) {
                filteredResult.add(dir);
            }
        }

        return filteredResult;
    }

    private String searchForParentWithStrutsNamespace(File currentFile, File basePath) {
        Pattern strutsKeywordMatcher = Pattern.compile("org\\.apache\\.struts[^\\.]", Pattern.CASE_INSENSITIVE);

        File currentFolder = currentFile;
        while (!currentFolder.getAbsolutePath().equals(basePath.getAbsolutePath())) {
            currentFolder = currentFolder.getParentFile();

            if (FileUtils.listFiles(currentFolder, new String[] { "java" }, true).isEmpty()) {
                continue;
            }

            boolean foundMatch = false;

            Collection<File> immediateFiles = FileUtils.listFiles(currentFolder, new String[] { "xml", "properties" }, false);
            for (File file : immediateFiles) {
                String fileContents;
                try {
                    fileContents = FileUtils.readFileToString(file);
                } catch (IOException e) {
                    fileContents = null;
                }

                if (fileContents != null && strutsKeywordMatcher.matcher(fileContents).find()) {
                    foundMatch = true;
                    break;
                }
            }

            if (foundMatch) {
                break;
            }
        }

        if (currentFolder.getAbsolutePath().equals(basePath.getAbsolutePath())) {
            return null;
        } else {
            return currentFolder.getAbsolutePath();
        }
    }
}
