package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.framework.filefilter.FileExtensionFileFilter;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Currently enumerates all Java files instead of filtering only for extenders of ie HttpServlet; keeping
//  this global instead of specific may help for non-standard servlet bases (ie a custom base extending HttpServlet)

public class JSPServletParser {

    private static final SanitizedLogger LOG = new SanitizedLogger("JSPServletParser");

    private List<JSPServlet> enumeratedServlets = list();

    private static Pattern packageNamePattern = Pattern.compile("package\\s+([^;]+);");
    private static Pattern accessServletRequestPattern = Pattern.compile("(\\w+)\\.getParameter\\(\"([^\"]+)\"\\)");
    private static Pattern declareServletRequestPattern = Pattern.compile("\\(\\s*HttpServletRequest\\s*(\\w+),");

    JSPServletParser(File rootDirectory) {
        loadServletsFromDirectory(rootDirectory);
    }

    private void loadServletsFromDirectory(File directory) {
        if (!directory.isDirectory())
            return;

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

            String packageName = parsePackageName(fileContents);
            Map<Integer, List<String>> queryParameters = parseParameters(fileContents);

            if (packageName == null) {
                LOG.debug("Couldn't detect package name for servlet at " + file.getAbsolutePath() + ", skipping that servlet");
                continue;
            }

            JSPServlet newServlet = new JSPServlet(packageName, servletName, file.getAbsolutePath(), queryParameters);
            enumeratedServlets.add(newServlet);
        }
    }

    String parsePackageName(String fileContents) {
        Matcher packageNameMatcher = packageNamePattern.matcher(fileContents);
        if (packageNameMatcher.find() && packageNameMatcher.groupCount() > 0) {
            return packageNameMatcher.group(1);
        }

        return null;
    }

    Map<Integer, List<String>> parseParameters(String fileContents) {
        List<String> requestVarNames = list();
        Map<Integer, List<String>> parameters = new HashMap<Integer, List<String>>();

        Matcher varNameMatcher = declareServletRequestPattern.matcher(fileContents);
        while (varNameMatcher.find()) {
            String varName = varNameMatcher.group(1);
            requestVarNames.add(varName);
        }

        String[] lines = fileContents.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];

            Matcher getParameterMatcher = accessServletRequestPattern.matcher(lineText);
            while (getParameterMatcher.find()) {
                String varName = getParameterMatcher.group(1);
                String parameterName = getParameterMatcher.group(2);

                if (requestVarNames.contains(varName)) {
                    int lineNumber = i;

                    if (!parameters.containsKey(lineNumber)) {
                        parameters.put(lineNumber, new ArrayList<String>());
                    }

                    List<String> knownParameters = parameters.get(lineNumber);
                    knownParameters.add(parameterName);
                }
            }
        }

        return parameters;
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
