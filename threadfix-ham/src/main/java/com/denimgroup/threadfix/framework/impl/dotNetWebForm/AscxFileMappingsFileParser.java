////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.framework.engine.ProjectDirectory;
import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.framework.util.CaseInsensitiveStringMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.util.CollectionUtils.stringMap;

/**
 * Created by mac on 10/24/14.
 */
public class AscxFileMappingsFileParser {

    private AscxFileMappingsFileParser(){}

    public static CaseInsensitiveStringMap<AscxFile> getMap(ProjectDirectory rootDirectory) {
        if (!rootDirectory.getDirectory().exists() || !rootDirectory.getDirectory().isDirectory()) {
            throw new IllegalArgumentException("Invalid directory passed to WebFormsEndpointGenerator: " + rootDirectory);
        }

        Collection ascxFiles = rootDirectory.findFiles("*.ascx");

        CaseInsensitiveStringMap<AscxFile> map = stringMap();

        for (Object aspxFile : ascxFiles) {
            if (aspxFile instanceof File) {

                File file = (File) aspxFile;
                String name = file.getName().toLowerCase(); // Normalize all names to lower-case since element name is case-insensitive
                String key = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
                map.put(key, new AscxFile(file));
            }
        }

        return map;
    }
}
