////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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

import com.denimgroup.threadfix.framework.util.PathUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Contains a set of web content files (HTML, JSP, CSS, etc.) for endpoint generation
public class StrutsWebPack {

    String absoluteRootDirectoryPath;
    List<String> relativeContentFilePaths = list();
    List<String> welcomeFiles = list();

    public StrutsWebPack(String absoluteRootDirectoryPath) {
        this.absoluteRootDirectoryPath = PathUtil.normalizeSeparator(absoluteRootDirectoryPath);
        if (!this.absoluteRootDirectoryPath.startsWith("/") && !new File(this.absoluteRootDirectoryPath).isAbsolute()) {
            this.absoluteRootDirectoryPath = "/" + this.absoluteRootDirectoryPath;
        }
    }

    public void addFile(String filePathRelativeToPackRoot) {
        relativeContentFilePaths.add(filePathRelativeToPackRoot);
    }

    public void addWelcomeFile(String fileName) {
        welcomeFiles.add(fileName);
    }

    public Collection<String> getRelativeFilePaths() {
        return relativeContentFilePaths;
    }

    public Collection<String> findContentsWithExt(String extension) {
        List<String> result = list();
        for (String path : relativeContentFilePaths) {
            if (path.endsWith(extension)) {
                result.add(path);
            }
        }
        return result;
    }

    public boolean contains(String filePathRelativeToPackRoot) {
        for (String file : relativeContentFilePaths) {
            if (file.equalsIgnoreCase(filePathRelativeToPackRoot)) {
                return true;
            }
        }
        return false;
    }

    public Collection<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    public String getRootDirectoryPath() {
        return absoluteRootDirectoryPath;
    }

}
