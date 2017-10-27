package com.denimgroup.threadfix.framework.impl.struts.plugins;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

//  See https://struts.apache.org/docs/rest-plugin.html

public class StrutsRestPluginDetector implements StrutsPluginDetectorImpl {

    private static final String REST_PLUGIN_KEYWORD = "struts2-rest-plugin";

    @Override
    public StrutsKnownPlugins getPluginType() {
        return StrutsKnownPlugins.STRUTS2_REST;
    }

    @Override
    public boolean detect(File projectRoot) {

        boolean wasDetected = false;
        Collection<File> files = FileUtils.listFiles(projectRoot, new String[] { "xml", "jar"}, true);

        File pomFile = null;
        File restLibFile = null;

        for (File file : files) {
            String fileName = file.getName();
            if (fileName.contains(REST_PLUGIN_KEYWORD)) {
                restLibFile = file;
            } else if (fileName.equals("pom.xml")) {
                pomFile = file;
            }
        }

        if (restLibFile != null) {
            wasDetected = true;
        } else if (pomFile != null) {
            wasDetected = pomContainsRestPlugin(pomFile);
        }

        return wasDetected;
    }

    private boolean pomContainsRestPlugin(File pomFile) {
        try {
            String fileContents = FileUtils.readFileToString(pomFile);
            return fileContents.contains(REST_PLUGIN_KEYWORD);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
