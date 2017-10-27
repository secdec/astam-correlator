package com.denimgroup.threadfix.framework.impl.struts.plugins;

import java.io.File;

public interface StrutsPluginDetectorImpl {

    StrutsKnownPlugins getPluginType();

    boolean detect(File projectRoot);

}
