package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.util.ScopeStringInterpolationDetector;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

public class CSharpInterpolationDetector implements ScopeStringInterpolationDetector {

    private ScopeTracker scopeTracker;
    private boolean isInterpolatingString = false;
    private int lastToken = 0;
    private boolean isInInterpolatableString = false;

    CSharpInterpolationDetector(ScopeTracker forTracker) {
        scopeTracker = forTracker;
    }

    @Override
    public boolean isInterpolatingString() {
        return isInterpolatingString;
    }

    @Override
    public void parseToken(int token) {
        if (!scopeTracker.isInString()) {
            isInInterpolatableString = false;
            isInterpolatingString = false;
            if (!(lastToken == '$' && token == '@')) {
                lastToken = token;
            }
            return;
        }

        if (lastToken == '$' && scopeTracker.enteredString()) {
            isInInterpolatableString = true;
        }

        if (isInInterpolatableString) {
            if (token == '{') {
                isInterpolatingString = true;
            } else if (token == '}') {
                isInterpolatingString = false;
            }
        }

        //  Capture interpolatable string of format $@"..."
        if (lastToken == '$' && token == '@') {
            return;
        }

        lastToken = token;
    }
}
