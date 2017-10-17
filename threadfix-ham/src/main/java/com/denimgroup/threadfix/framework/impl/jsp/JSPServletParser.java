package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Currently enumerates all Java files instead of filtering only for extenders of ie HttpServlet; keeping
//  this global instead of specific may help for non-standard servlet bases (ie a custom base extending HttpServlet)

public class JSPServletParser {
    private List<JSPServlet> enumeratedServlets = list();

    JSPServletParser(File rootDirectory) {
        loadServletsFromDirectory(rootDirectory);
    }

    private void loadServletsFromDirectory(File directory) {
        if (!directory.isDirectory())
            return;

        Pattern packageNamePattern = Pattern.compile("package\\s+([^;]+);");

        Collection<File> files = FileUtils.listFiles(directory, new String[] { "java" }, true);
        for (File file : files) {

            String servletName = file.getName();
            servletName = servletName.replace(".java", "");

            String fileContents;

            try {
                fileContents = FileUtils.readFileToString(file);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            Matcher packageNameMatcher = packageNamePattern.matcher(fileContents);
            if (packageNameMatcher.find() && packageNameMatcher.groupCount() > 0) {
                String servletPackage = packageNameMatcher.group(1);

                JSPServlet newServlet = new JSPServlet(servletPackage, servletName, file.getAbsolutePath());
                enumeratedServlets.add(newServlet);
            }
        }
    }

    public List<JSPServlet> getServlets() {
        return enumeratedServlets;
    }

    //  Absolute name as <package>.<classname> ie "com.my.package.MyClass"

    public JSPServlet findServletByAbsoluteName(String absoluteName) {
        for (JSPServlet servlet : enumeratedServlets) {
            if (servlet.getAbsoluteName().equals(absoluteName)) {
                return servlet;
            }
        }

        return null;
    }
}
