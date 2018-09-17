package com.denimgroup.threadfix.framework.util;

public interface ScopeStringInterpolationDetectorFactory {
    ScopeStringInterpolationDetector makeDetector(ScopeTracker forScopeTracker);
}
