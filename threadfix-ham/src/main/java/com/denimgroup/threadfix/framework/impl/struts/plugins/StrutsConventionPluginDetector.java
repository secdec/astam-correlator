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

package com.denimgroup.threadfix.framework.impl.struts.plugins;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class StrutsConventionPluginDetector implements StrutsPluginDetectorImpl {

    private final String CONVENTION_PLUGIN_KEYWORD = "struts2-convention-plugin";

    @Override
    public StrutsPlugin create() {
        return new StrutsConventionPlugin();
    }

    @Override
    public boolean detect(File projectRoot) {

        boolean wasDetected = false;

        Collection<File> files = FileUtils.listFiles(projectRoot, new String[] { "xml", "jar" }, true);

        File pomFile = null;
        File conventionLibFile = null;

        for (File file : files) {
            String fileName = file.getName();

            if (fileName.equals("pom.xml")) {
                pomFile = file;
            } else if (fileName.contains(CONVENTION_PLUGIN_KEYWORD)) {
                conventionLibFile = file;
            }
        }

        if (conventionLibFile != null) {
            wasDetected = true;
        } else if (pomFile != null) {
            wasDetected = pomContainsConventionPlugin(pomFile);
        }

        return wasDetected;
    }

    private boolean pomContainsConventionPlugin(File pomFile) {
        try {
            String fileContents = FileUtils.readFileToString(pomFile);
            return fileContents.contains(CONVENTION_PLUGIN_KEYWORD);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
