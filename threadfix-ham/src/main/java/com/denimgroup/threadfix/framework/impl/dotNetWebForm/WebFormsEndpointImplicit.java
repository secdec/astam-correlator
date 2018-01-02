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
package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.File;

/*
 * Represents endpoints without any resource name, such as /some/path/. A new
 * implicit endpoint does not validate whether or not the input path is valid
 * as an implicit endpoint.
 */
public class WebFormsEndpointImplicit extends WebFormsEndpointBase {

    private static final SanitizedLogger LOG = new SanitizedLogger(WebFormsEndpointImplicit.class);

    public WebFormsEndpointImplicit(File aspxRoot, AspxParser aspxParser, AspxCsParser aspxCsParser) {
        super(aspxRoot, aspxParser, aspxCsParser);
    }

    @Override
    protected String calculateUrlPath() {
        String aspxFilePath = aspxParser.file.getAbsolutePath();
        String aspxRootPath = aspxRoot.getAbsolutePath();

        String relativePath = calculateRelativePath(aspxFilePath, aspxRootPath);
        int lastPathIndex = relativePath.lastIndexOf('/');

        return relativePath.substring(0, lastPathIndex + 1);
    }
}
