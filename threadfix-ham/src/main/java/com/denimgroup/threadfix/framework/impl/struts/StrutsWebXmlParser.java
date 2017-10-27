package com.denimgroup.threadfix.framework.impl.struts;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class StrutsWebXmlParser {


    List<String> welcomeFiles = list();
    String webXmlFileLocation = null;


    public static File findWebXml(File rootSearchDirectory) {
        Collection<File> xmlFiles = FileUtils.listFiles(rootSearchDirectory, new String[] { "xml" }, true);
        for (File file : xmlFiles) {
            if (file.getName().equalsIgnoreCase("web.xml")) {
                return file;
            }
        }
        return null;
    }

    public StrutsWebXmlParser(File webXmlFile) {
        String webXmlContents;
        try {
            webXmlContents = FileUtils.readFileToString(webXmlFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Pattern welcomeFilePattern = Pattern.compile("<welcome-file>(.*)<\\/welcome-file>");
        Matcher welcomeFileMatcher = welcomeFilePattern.matcher(webXmlContents);

        while (welcomeFileMatcher.find()) {
            welcomeFiles.add(welcomeFileMatcher.group(1));
        }

        webXmlFileLocation = webXmlFile.getAbsolutePath();
    }

    public Collection<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    public String getPrimaryWebContentPath() {
        return trimPathEnd(webXmlFileLocation, 2);
    }

    public String getWebInfFolderPath() {
        return trimPathEnd(webXmlFileLocation, 1);
    }



    String trimPathEnd(String path, int numTrimmedParts) {
        String[] pathParts = path.split("\\/");
        StringBuilder result = new StringBuilder();

        int endIndex = pathParts.length - numTrimmedParts;
        for (int i = 0; i < endIndex; i++) {
            if (i > 0) {
                result.append("/");
            }

            result.append(pathParts[i]);
        }

        return result.toString();
    }

}
