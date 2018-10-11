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

import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsWebXmlParser {


    List<String> welcomeFiles = list();
    String webXmlFileLocation = null;


    public static File findWebXml(CachedDirectory rootSearchDirectory) {
        Collection<File> xmlFiles = rootSearchDirectory.findFiles("*.xml");
        Collection<File> webXmlFiles = list();
        for (File file : xmlFiles) {
            if (file.getName().equalsIgnoreCase("web.xml")) {
                webXmlFiles.add(file);
            }
        }

        /*  Find best web.xml match */

        //  Check if it contains 'webapp'
        for (File file : webXmlFiles) {
            if (file.getAbsolutePath().toLowerCase().contains("webapp")) {
                return file;
            }
        }

        //  Otherwise select the one with the shortest path
        File shortestPathFile = null;
        for (File file : webXmlFiles) {
            if (shortestPathFile == null || file.getAbsolutePath().length() < shortestPathFile.getAbsolutePath().length()) {
                shortestPathFile = file;
            }
        }

        return shortestPathFile;
    }

    public StrutsWebXmlParser(File webXmlFile) {
        String webXmlContents;
        try {
            webXmlContents = FileUtils.readFileToString(webXmlFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Pattern welcomeFilePattern = Pattern.compile("<welcome-file>(.*)<\\/welcome-file>");
        Matcher welcomeFileMatcher = welcomeFilePattern.matcher(webXmlContents);

        while (welcomeFileMatcher.find()) {
            welcomeFiles.add(welcomeFileMatcher.group(1));
        }

        webXmlFileLocation = webXmlFile.getAbsolutePath();
    }

    public Collection<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    public String getPrimaryWebContentPath() {
        return trimPathEnd(webXmlFileLocation, 2);
    }

    public String getWebInfFolderPath() {
        return trimPathEnd(webXmlFileLocation, 1);
    }



    String trimPathEnd(String path, int numTrimmedParts) {
        String[] pathParts = path.split("[\\/\\\\]");
        StringBuilder result = new StringBuilder();

        int endIndex = pathParts.length - numTrimmedParts;
        for (int i = 0; i < endIndex; i++) {
            if (i > 0) {
                result.append("/");
            }

            result.append(pathParts[i]);
        }

        return result.toString();
    }

}
