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

import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.denimgroup.threadfix.framework.util.CaseInsensitiveStringMap;

import java.io.File;
import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.util.CollectionUtils.stringMap;

/**
 * Created by mac on 10/27/14.
 */
public class MasterPageParser {

    private MasterPageParser(){}

    public static CaseInsensitiveStringMap<AspxParser> getMasterFileMap(CachedDirectory rootDirectory) {
        CaseInsensitiveStringMap<AscxFile> map = AscxFileMappingsFileParser.getMap(rootDirectory);
        return getMasterFileMap(rootDirectory, map);
    }

    public static CaseInsensitiveStringMap<AspxParser> getMasterFileMap(CachedDirectory rootDirectory, CaseInsensitiveStringMap<AscxFile> ascxFileMap) {
        if (rootDirectory == null) {
            throw new IllegalArgumentException("Can't pass null argument to getMasterFileMap()");
        } else if (!rootDirectory.getDirectory().isDirectory()) {
            throw new IllegalArgumentException("Can't pass a non-directory file argument to getMasterFileMap()");
        }

        CaseInsensitiveStringMap<AspxParser> parserMap = stringMap();

        Collection masterFiles = rootDirectory.findFiles("*.master");

        for (Object aspxFile : masterFiles) {
            if (aspxFile instanceof File) {
                File file = (File) aspxFile;

                AspxParser aspxParser = AspxParser.parse(file);
                AspxUniqueIdParser uniqueIdParser = AspxUniqueIdParser.parse(file, ascxFileMap);

                aspxParser.parameters.addAll(uniqueIdParser.parameters);
                parserMap.put(file.getName(), aspxParser);
            }
        }

        return parserMap;
    }
}
