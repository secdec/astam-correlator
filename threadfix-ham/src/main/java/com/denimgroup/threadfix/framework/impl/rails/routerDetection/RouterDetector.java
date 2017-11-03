package com.denimgroup.threadfix.framework.impl.rails.routerDetection;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouterDetector {

    static final Collection<RouterDetectorImpl> routerDetectors = list((RouterDetectorImpl)new DeviseRouterDetector());

    public Collection<RailsRouter> detectRouters(File gemFile) {
        List<RailsRouter> routers = list();

        Collection<String> lines;
        try {
            lines = FileUtils.readLines(gemFile);
        } catch (IOException e) {
            e.printStackTrace();
            return routers;
        }

        for (String line : lines) {
            for (RouterDetectorImpl detector : routerDetectors) {
                if (detector.detect(line)) {
                    routers.add(detector.makeRouter());
                    break;
                }
            }
        }

        return routers;
    }
}
