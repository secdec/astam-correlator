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

package com.denimgroup.threadfix.framework.impl.struts;


import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

// Used to detect the set of Struts project folders contained within a given directory
public class StrutsProjectDetector {
    public List<String> findProjectPaths(CachedDirectory baseDirectory) {

        /* Search for struts-compatible XML files, and traverse parent directories from there until the parent directory
         *  contains java files. That parent directory will be a project root.
         */

        List<String> result = list();
        Collection<File> xmlFiles = baseDirectory.findFiles("*.xml");
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
                String possibleProjectRoot = searchForParentWithStrutsNamespace(xmlFile, baseDirectory.getDirectory(), baseDirectory);
                if (possibleProjectRoot != null && !result.contains(possibleProjectRoot)) {
                    result.add(possibleProjectRoot);
                }
            }
        }

        if (result.size() == 0) {
            result.add(baseDirectory.getDirectory().getAbsolutePath());
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

    private String searchForParentWithStrutsNamespace(File currentFile, File basePath, CachedDirectory cachedDirectory) {
        Pattern strutsKeywordMatcher = Pattern.compile("org\\.apache\\.struts[^\\.]", Pattern.CASE_INSENSITIVE);

        File currentFolder = currentFile;
        while (!currentFolder.getAbsolutePath().equals(basePath.getAbsolutePath())) {
            currentFolder = currentFolder.getParentFile();

            if (cachedDirectory.findFilesIn(currentFolder.getAbsolutePath(), "*.java").isEmpty()) {
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
