package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

public class DotNetCoreDetector implements EventBasedTokenizer {
    private boolean shouldContinue = true;
    private boolean isCore = false;

    public boolean isAspDotNetCore() {
        return isCore;
    }

    @Override
    public boolean shouldContinue() {
        return shouldContinue;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (stringValue.contains("Microsoft.AspNetCore.")) {
                isCore = true;
                shouldContinue = false;
            } else if (stringValue.contains("Microsoft.AspNet.")) {
                isCore = false;
                shouldContinue = false;
            }
        }
    }
}
