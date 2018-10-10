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

import com.denimgroup.threadfix.framework.util.ScopeTracker;
import com.denimgroup.threadfix.framework.util.ScopeVerbatimStringDetector;

/*
 * Note - This cannot handle quotes in C# verbatim strings. These are formatted:
 *
 * "Look ""ma""!" == Look "ma"!
 *
 * Supporting this requires either token lookahead (unsupported with current EventBasedTokenizer)
 * or changing ScopeDetector string detection timing (ie set "exitedString" after we've passed
 * the closing quote rather than once we've reached it) which will require a bunch of other
 * changes in code that depends on ScopeTracker.
 */

public class CSharpVerbatimStringDetector implements ScopeVerbatimStringDetector {
    private boolean isInVerbatimString = false;
    private int lastToken = -1;
    private ScopeTracker scopeTracker;

    public CSharpVerbatimStringDetector(ScopeTracker scopeTracker) {
        this.scopeTracker = scopeTracker;
    }

    @Override
    public boolean isInVerbatimString() {
        return isInVerbatimString;
    }

    @Override
    public void parseToken(int token) {
        if (!scopeTracker.isInString()) {
            isInVerbatimString = false;
            lastToken = token;
            return;
        }

        if (lastToken == '@') {
            isInVerbatimString = true;
        }
        lastToken = token;
    }
}
