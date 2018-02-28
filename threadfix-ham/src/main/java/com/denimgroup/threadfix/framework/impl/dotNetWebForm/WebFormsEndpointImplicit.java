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

import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.*;

/*
 * Represents endpoints without any resource name, such as /some/path/. A new
 * implicit endpoint does not validate whether or not the input path is valid
 * as an implicit endpoint.
 */
public class WebFormsEndpointImplicit extends WebFormsEndpointBase {

    private static final SanitizedLogger LOG = new SanitizedLogger(WebFormsEndpointImplicit.class);

    private WebFormsEndpointImplicit() {

    }

    public WebFormsEndpointImplicit(File aspxRoot, AspxParser aspxParser, AspxCsParser aspxCsParser) {
        super(aspxRoot, aspxParser, aspxCsParser);
    }

    @Override
    protected String calculateUrlPath() {
        String relativePath = calculateRelativePath(aspxFilePath, aspxRoot);
        int lastPathIndex = relativePath.lastIndexOf('/');

        return relativePath.substring(0, lastPathIndex + 1);
    }
}
