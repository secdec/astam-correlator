package com.denimgroup.threadfix.framework.impl.struts;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class StrutsWebPackBuilder {

    static final String[] RESERVED_FILE_NAMES = new String[] { "WEB-INF", "META-INF" };

    public StrutsWebPack generate(File contentRoot) {

        StrutsWebPack result = new StrutsWebPack(contentRoot.getAbsolutePath());

        Collection<File> files = FileUtils.listFiles(contentRoot, null, true);
        for (File contentFile : files) {
            String relativePath = makeRelativePath(contentRoot.getAbsolutePath(), contentFile.getAbsolutePath());

            boolean parse = true;
            for (String reservedName : RESERVED_FILE_NAMES) {
                if (relativePath.contains(reservedName)) {
                    parse = false;
                    break;
                }
            }

            if (!parse) {
                continue;
            }

            result.addFile(relativePath);
        }

        return result;
    }

    String makeRelativePath(String rootPath, String absolutePath) {
        String result = absolutePath.replaceAll("^" + rootPath, "");
        if (!result.startsWith("/"))
            result = "/" + result;
        return result;
    }

}
