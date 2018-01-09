package com.denimgroup.threadfix.framework.impl.struts.plugins;

import java.io.File;

public interface StrutsPluginDetectorImpl {
    StrutsPlugin create();
    boolean detect(File projectRoot);

}
