package com.denimgroup.threadfix.framework.impl.struts.plugins;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class StrutsRestPluginDetector implements StrutsPluginDetectorImpl {
    @Override
    public StrutsPlugin create() {
        return new StrutsRestPlugin();
    }

    private final String REST_PLUGIN_KEYWORD = "struts2-rest-plugin";

    @Override
    public boolean detect(File projectRoot) {

        boolean wasDetected = false;

        Collection<File> files = FileUtils.listFiles(projectRoot, new String[] { "xml", "jar" }, true);

        File pomFile = null;
        File conventionLibFile = null;

        for (File file : files) {
            String fileName = file.getName();

            if (fileName.equals("pom.xml")) {
                pomFile = file;
            } else if (fileName.contains(REST_PLUGIN_KEYWORD)) {
                conventionLibFile = file;
            }
        }

        if (conventionLibFile != null) {
            wasDetected = true;
        } else if (pomFile != null) {
            wasDetected = pomContainsConventionPlugin(pomFile);
        }

        return wasDetected;
    }

    private boolean pomContainsConventionPlugin(File pomFile) {
        try {
            String fileContents = FileUtils.readFileToString(pomFile);
            return fileContents.contains(REST_PLUGIN_KEYWORD);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
