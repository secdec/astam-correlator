////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.engine.CachedDirectory;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.ScopeTracker;
import com.denimgroup.threadfix.framework.util.java.CommentTracker;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  Currently enumerates all Java files instead of filtering only for extenders of ie HttpServlet; keeping
//  this global instead of specific may help for non-standard servlet bases (ie a custom base extending HttpServlet)

public class JSPServletParser {

    public static boolean isServlet(File file) {
        if (!file.isFile())
            return false;

        String fileContents;
        try {
            fileContents = FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return servletPattern.matcher(fileContents).find();
    }

    private static final SanitizedLogger LOG = new SanitizedLogger("JSPServletParser");

    private List<JSPServlet> enumeratedServlets = list();

    private static Pattern servletPattern = Pattern.compile("extends\\s+HttpServlet");
    private static Pattern packageNamePattern = Pattern.compile("package\\s+([^;]+);");
    private static Pattern accessServletRequestPattern = Pattern.compile("(\\w+)\\.getParameter\\(\"([^\"]+)\"\\)");
    private static Pattern declareServletRequestPattern = Pattern.compile("\\(\\s*HttpServletRequest\\s*(\\w+),");
    private static Pattern responseMethodPattern = Pattern.compile("\\s+(\\w+)\\s*\\(\\s*HttpServletRequest");

    //  ie @WebServlet(urlPatterns = {"/abc", "/def"}) or @WebServlet(value = "/abc")
    private static Pattern annotatedWebServletManyUrlTypedPattern = Pattern.compile("urlPatterns\\s*=\\s*\\{([^\\}]+)\\}");

    //  ie @WebServlet({"/abc, "/def"}) or @WebServlet("/abc")
    private static Pattern annotatedWebServletSingleUrlTypedPattern = Pattern.compile("value\\s*=\\s*([^,]+)");

    private static List<String> SERVLET_RESPONSE_METHODS = list("doGet", "doPut", "doPost", "doDelete");


    JSPServletParser(File rootDirectory) {
        loadServletsFromDirectory(rootDirectory);
    }

    private void loadServletsFromDirectory(File directory) {
        if (!directory.isDirectory())
            return;

        CachedDirectory cachedDirectory = new CachedDirectory(directory);

        Collection<File> files = cachedDirectory.findFiles("*.java");
        for (File file : files) {

            if (!isServlet(file))
                continue;

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
            Map<Integer, List<RouteParameter>> queryParameters = parseParameters(fileContents);
            List<String> annotatedEndpoints = parseAnnotatedEndpoints(fileContents);
            List<JSPServletMethodMap> servletQueryMethods = parseResponseMethodMap(fileContents);

            if (packageName == null) {
                LOG.debug("Couldn't detect package name for servlet at " + file.getAbsolutePath() + ", skipping that servlet");
                continue;
            }

            Map<String, List<RouteParameter>> methodMappedParameters = map();
            Map<String, Integer> methodStartLines = map();
            Map<String, Integer> methodEndLines = map();

            for (JSPServletMethodMap methodMap : servletQueryMethods) {
                int startLine = methodMap.startLine;
                int endLine = methodMap.endLine;
                List<RouteParameter> relevantParameters = list();
                for (Map.Entry<Integer, List<RouteParameter>> lineParams : queryParameters.entrySet()) {
                    int lineNo = lineParams.getKey();
                    if (lineNo >= startLine && lineNo <= endLine) {
                        relevantParameters.addAll(lineParams.getValue());
                    }
                }

                String method = methodMap.methodName;
                if (!SERVLET_RESPONSE_METHODS.contains(method)) {
                    continue;
                }
                String httpMethod = method.replace("do", "").toUpperCase();
                methodMappedParameters.put(httpMethod, relevantParameters);

                methodStartLines.put(httpMethod, methodMap.startLine);
                methodEndLines.put(httpMethod, methodMap.endLine);
            }

            JSPServlet newServlet = new JSPServlet(packageName, servletName, FilePathUtils.normalizePath(file.getAbsolutePath()), queryParameters);
            for (String annotation : annotatedEndpoints) {
                newServlet.addEndpoint(annotation);
            }
            for (String httpMethod : methodMappedParameters.keySet()) {
            	newServlet.addHttpMethod(httpMethod, methodStartLines.get(httpMethod), methodEndLines.get(httpMethod));
                for (RouteParameter param : methodMappedParameters.get(httpMethod)) {
                    newServlet.addParameter(httpMethod, param);
                }
            }

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

    Map<Integer, List<RouteParameter>> parseParameters(String fileContents) {
        // Names of the 'HttpServletRequest' objects that are being accessed for parameters by the client code
        List<String> requestVarNames = list();
        Map<Integer, List<RouteParameter>> parameters = new HashMap<Integer, List<RouteParameter>>();

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
                        parameters.put(lineNumber, new ArrayList<RouteParameter>());
                    }

                    List<RouteParameter> knownParameters = parameters.get(lineNumber);
                    RouteParameter newParameter = new RouteParameter(parameterName);

                    knownParameters.add(newParameter);
                }
            }
        }

        return parameters;
    }

    List<String> parseAnnotatedEndpoints(String fileContents) {
        String atWebServlet = "@WebServlet";

        StringBuilder annotationParts = new StringBuilder();

        String[] lines = fileContents.split("\n");

        List<String> servletAnnotationParameters = new ArrayList<String>();

        boolean isMatchingAnnotation = false;
        boolean matchStartsThisLine;
        int numParens = 0;

        for (String line : lines) {
            matchStartsThisLine = false;

            if (!isMatchingAnnotation) {
                if (!line.contains(atWebServlet)) {
                    continue;
                }
                else {
                    matchStartsThisLine = true;
                    isMatchingAnnotation = true;
                }
            }

            int i;

            if (matchStartsThisLine) {
                i = line.indexOf(atWebServlet) + atWebServlet.length();
            } else {
                i = 0;
            }

            for (; isMatchingAnnotation && i < line.length(); i++) {

                char c = line.charAt(i);

                if (c == ')') {
                    numParens--;
                }

                if (c == '(') {
                    if (numParens++ == 0)
                        continue;
                }

                if (c == '\n') {
                    continue;
                }

                if (numParens <= 0) {
                    isMatchingAnnotation = false;

                    servletAnnotationParameters.add(annotationParts.toString());
                    annotationParts = new StringBuilder();

                } else {
                    annotationParts.append(c);
                }
            }
        }

        if (servletAnnotationParameters.size() > 1) {
            LOG.debug("Detected more than one @WebServlet annotation, only using the first one");
        } else if (servletAnnotationParameters.size() == 0) {
            return list();
        }

        List<String> mappedUrls = list();

        String params = servletAnnotationParameters.get(0).trim();

        //  Multi-URL implicit parameter notation @WebServlet({ "/a", "/b"})
        if (params.startsWith("{") && params.endsWith("}")) {
            params = params.replaceAll("\\{", "").replaceAll("\\}", "");

            String[] paramParts = params.split(",");
            for (String part : paramParts) {
                part = part.trim();
                if (part.startsWith("\"")) {
                    part = part.substring(1);
                }
                if (part.endsWith("\"")) {
                    part = part.substring(0, part.length() - 1);
                }

                mappedUrls.add(part);
            }
        }
        //  Single-URL implicit parameter notation @WebServlet("/a")
        else if (params.startsWith("\"") && params.endsWith("\"")) {
            mappedUrls.add(params.substring(1, params.length() - 1));
        }
        //  Multi-URL named parameter notation @WebServlet(urlPatterns={"/a", "/b"})
        else if (params.contains("urlPatterns")) {
            Matcher multiUrlNamedMatcher = annotatedWebServletManyUrlTypedPattern.matcher(params);
            if (!multiUrlNamedMatcher.find()) {
                LOG.debug("Couldn't match urlPatterns parameter against @WebServlet");
            } else {
                String urlPatternsValueText = multiUrlNamedMatcher.group(1);
                String[] patternsParts = urlPatternsValueText.split(",");

                for (String part : patternsParts) {
                    part = part.trim();
                    if (part.startsWith("\"")) {
                        part = part.substring(1);
                    }
                    if (part.endsWith("\"")) {
                        part = part.substring(0, part.length() - 1);
                    }

                    mappedUrls.add(part);
                }
            }
        }
        //  Single-URL named parameter notation @WebServlet(value="/a")
        else if (params.contains("value")) {
            Matcher singleUrlNamedMatcher = annotatedWebServletSingleUrlTypedPattern.matcher(params);
            if (!singleUrlNamedMatcher.find()) {
                LOG.debug("Couldn't match value parameter against @WebServlet");
            } else {
                if (params.startsWith("\"")) {
                    params = params.substring(1);
                }
                if (params.endsWith("\"")) {
                    params = params.substring(params.length() - 1);
                }

                mappedUrls.add(params);
            }
        }

        return mappedUrls;

    }

    private List<JSPServletMethodMap> parseResponseMethodMap(String fileContents) {
        List<JSPServletMethodMap> result = list();

        ScopeTracker scopeTracker = new ScopeTracker();
        CommentTracker commentTracker = new CommentTracker();
        int currentScanningIndex = 0;
        int lineNo = 1;

        Matcher responseMethodMatcher = responseMethodPattern.matcher(fileContents);
        while (responseMethodMatcher.find()) {
            String methodName = responseMethodMatcher.group(1);
            int stringStartIndex = responseMethodMatcher.start();
            int methodStartLine = -1;
            int methodEndLine = -1;

            while (currentScanningIndex < stringStartIndex) {
                char c = fileContents.charAt(currentScanningIndex++);
                if (c == '\n') {
                    ++lineNo;
                }

                if (!scopeTracker.isInString()) {
                    commentTracker.interpretToken(c);
                }

                if (!commentTracker.isInComment()) {
                    scopeTracker.interpretToken(c);
                }
            }

            methodStartLine = lineNo;

            int currentMethodBraceLevel = scopeTracker.getNumOpenBrace();
            boolean enteredMethodBody = false;
            boolean isAbstract = false;
            while (currentMethodBraceLevel != scopeTracker.getNumOpenBrace() || !enteredMethodBody) {
                if (currentMethodBraceLevel != scopeTracker.getNumOpenBrace()) {
                    enteredMethodBody = true;
                }
                char c = fileContents.charAt(currentScanningIndex++);
                if (c == ';' && !enteredMethodBody) {
                    // must be an abstract method
                    isAbstract = true;
                    break;
                }
                if (c == '\n') {
                    ++lineNo;
                }
                if (!scopeTracker.isInString()) {
                    commentTracker.interpretToken(c);
                }

                if (!commentTracker.isInComment()) {
                    scopeTracker.interpretToken(c);
                }
            }

            methodEndLine = lineNo;

            if (!isAbstract) {
                JSPServletMethodMap newMap = new JSPServletMethodMap();
                newMap.methodName = methodName;
                newMap.startLine = methodStartLine;
                newMap.endLine = methodEndLine;

                result.add(newMap);
            }
        }

        return result;
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
