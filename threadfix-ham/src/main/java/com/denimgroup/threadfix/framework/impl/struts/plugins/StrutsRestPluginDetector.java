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

import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class StrutsRestPluginDetector implements StrutsPluginDetectorImpl {
    @Override
    public StrutsPlugin create() {
        return new StrutsRestPlugin();
    }

    private final String REST_PLUGIN_KEYWORD = "struts2-rest-plugin";

    @Override
    public boolean detect(CachedDirectory projectRoot) {

        Collection<File> files = projectRoot.findFiles("*.xml", "*.properties", "*.jar");

        for (File file : files) {
            String fileName = file.getName();

            if (!fileName.endsWith("jar")) {
                if (fileReferencesRestPlugin(file)) {
                    return true;
                }
            } else if (fileName.contains(REST_PLUGIN_KEYWORD)) {
                return true;
            }
        }

        return false;
    }

    private boolean fileReferencesRestPlugin(File configFile) {
        try {
            String fileContents = FileUtils.readFileToString(configFile);
            return fileContents.contains(REST_PLUGIN_KEYWORD);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
