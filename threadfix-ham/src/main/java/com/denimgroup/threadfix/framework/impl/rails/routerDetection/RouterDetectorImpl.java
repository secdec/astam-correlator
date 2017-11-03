package com.denimgroup.threadfix.framework.impl.rails.routerDetection;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;

import java.io.File;

public interface RouterDetectorImpl {
    boolean detect(String gemFileLineEntry);
    RailsRouter makeRouter();
}
