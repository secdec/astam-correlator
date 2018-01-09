package com.denimgroup.threadfix.framework.impl.struts;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Contains a set of web content files (HTML, JSP, CSS, etc.) for endpoint generation
public class StrutsWebPack {

    String absoluteRootDirectoryPath;
    List<String> relativeContentFilePaths = list();
    List<String> welcomeFiles = list();

    public StrutsWebPack(String absoluteRootDirectoryPath) {
        this.absoluteRootDirectoryPath = absoluteRootDirectoryPath;
        if (!this.absoluteRootDirectoryPath.startsWith("/")) {
            this.absoluteRootDirectoryPath = "/" + this.absoluteRootDirectoryPath;
        }
    }

    public void addFile(String filePathRelativeToPackRoot) {
        relativeContentFilePaths.add(filePathRelativeToPackRoot);
    }

    public void addWelcomeFile(String fileName) {
        welcomeFiles.add(fileName);
    }

    public Collection<String> getRelativeFilePaths() {
        return relativeContentFilePaths;
    }

    public Collection<String> findContentsWithExt(String extension) {
        List<String> result = list();
        for (String path : relativeContentFilePaths) {
            if (path.endsWith(extension)) {
                result.add(path);
            }
        }
        return result;
    }

    public boolean contains(String filePathRelativeToPackRoot) {
        for (String file : relativeContentFilePaths) {
            if (file.equalsIgnoreCase(filePathRelativeToPackRoot)) {
                return true;
            }
        }
        return false;
    }

    public Collection<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    public String getRootDirectoryPath() {
        return absoluteRootDirectoryPath;
    }

}
