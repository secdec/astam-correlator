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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsWebPackBuilder {

    static final String[] RESERVED_FILE_NAMES = new String[] { "WEB-INF", "META-INF" };

    private List<String> acceptedFileTypes = list();

    public StrutsWebPack generate(File contentRoot) {

        StrutsWebPack result = new StrutsWebPack(contentRoot.getAbsolutePath());

        String[] fileExtensions;
        if (acceptedFileTypes.isEmpty()) {
            fileExtensions = null;
        } else {
            fileExtensions = acceptedFileTypes.toArray(new String[acceptedFileTypes.size()]);
        }

        Collection<File> files = FileUtils.listFiles(contentRoot, fileExtensions, true);
        for (File contentFile : files) {
            String relativePath = makeRelativePath(contentRoot.getAbsolutePath(), contentFile.getAbsolutePath());

            boolean parse = true;
            for (String reservedName : RESERVED_FILE_NAMES) {
                if (relativePath.contains(reservedName)) {
                    parse = false;
                }
            }

            if (!parse) {
                continue;
            }

            result.addFile(relativePath);
        }

        return result;
    }

    public void acceptFileType(String ...fileTypes) {
        for (String ext : fileTypes) {
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            acceptedFileTypes.add(ext);
        }
    }

    String makeRelativePath(String rootPath, String absolutePath) {

        String[] rootParts = rootPath.split("[\\\\\\/]");
        String[] pathParts = absolutePath.split("[\\\\\\/]");

        int startIndex = 0;
        for (; startIndex < rootParts.length && startIndex < pathParts.length; startIndex++) {
            if (!rootParts[startIndex].equalsIgnoreCase(pathParts[startIndex])) {
                break;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < pathParts.length; i++) {
            result.append('/');
            result.append(pathParts[i]);
        }

        return result.toString();
    }

}
