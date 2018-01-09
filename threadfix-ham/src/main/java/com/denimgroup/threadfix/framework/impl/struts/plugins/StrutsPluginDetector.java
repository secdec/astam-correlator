package com.denimgroup.threadfix.framework.impl.struts.plugins;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsPluginDetector {

    public Collection<StrutsPlugin> detectPlugins(File projectRoot) {

        Collection<StrutsPluginDetectorImpl> detectors = list(
                new StrutsConventionPluginDetector(),
                new StrutsRestPluginDetector()
        );

        List<StrutsPlugin> plugins = list();
        for (StrutsPluginDetectorImpl detector : detectors) {
            if (detector.detect(projectRoot)) {
                plugins.add(detector.create());
            }
        }

        return plugins;
    }

}
