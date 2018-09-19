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
