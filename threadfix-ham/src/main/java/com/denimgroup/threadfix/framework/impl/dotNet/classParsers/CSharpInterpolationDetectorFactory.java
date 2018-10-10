////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.util.ScopeStringInterpolationDetector;
import com.denimgroup.threadfix.framework.util.ScopeStringInterpolationDetectorFactory;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

public class CSharpInterpolationDetectorFactory implements ScopeStringInterpolationDetectorFactory {
    @Override
    public ScopeStringInterpolationDetector makeDetector(ScopeTracker forScopeTracker) {
        return new CSharpInterpolationDetector(forScopeTracker);
    }
}
