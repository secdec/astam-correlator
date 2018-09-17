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
