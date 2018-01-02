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

package com.denimgroup.threadfix.framework.impl.rails.routerDetection;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouterDetector {

    static final Collection<RouterDetectorImpl> routerDetectors = list((RouterDetectorImpl)new DeviseRouterDetector());

    public Collection<RailsRouter> detectRouters(File gemFile) {
        List<RailsRouter> routers = list();

        Collection<String> lines;
        try {
            lines = FileUtils.readLines(gemFile);
        } catch (IOException e) {
            e.printStackTrace();
            return routers;
        }

        for (String line : lines) {
            for (RouterDetectorImpl detector : routerDetectors) {
                if (detector.detect(line)) {
                    routers.add(detector.makeRouter());
                    break;
                }
            }
        }

        return routers;
    }
}
