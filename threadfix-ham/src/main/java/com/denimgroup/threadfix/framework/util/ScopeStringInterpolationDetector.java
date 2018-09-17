package com.denimgroup.threadfix.framework.util;

public interface ScopeStringInterpolationDetector {
    boolean isInterpolatingString();
    void parseToken(int token);
}
