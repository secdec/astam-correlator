package com.denimgroup.threadfix.framework.impl.rails.routerDetection;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.DeviseRouter;

public class DeviseRouterDetector implements RouterDetectorImpl {
    @Override
    public boolean detect(String gemFileLineEntry) {
        return gemFileLineEntry.contains("devise");
    }

    @Override
    public RailsRouter makeRouter() {
        return new DeviseRouter();
    }
}
