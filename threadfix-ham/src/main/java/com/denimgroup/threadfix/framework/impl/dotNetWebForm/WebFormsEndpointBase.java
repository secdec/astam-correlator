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
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.*;

/*
 * Represents endpoints without any resource name, such as /some/path/.
 */
abstract class WebFormsEndpointBase extends AbstractEndpoint {

    private static final SanitizedLogger LOG = new SanitizedLogger(WebFormsEndpointBase.class);

    final AspxParser   aspxParser;
    final AspxCsParser aspxCsParser;
    final File         aspxRoot;
    final String       urlPath;
    final String       filePath;

    Map<String, List<Integer>> map = map();
    private Set<String> httpMethods;

    public WebFormsEndpointBase(File aspxRoot, AspxParser aspxParser, AspxCsParser aspxCsParser) {
        if (!checkArguments(aspxParser.aspName, aspxCsParser.aspName)) {
            throw new IllegalArgumentException("Invalid aspx mappings pairs passed to WebFormsEndpointBase constructor: " +
                    aspxParser.aspName + " and " + aspxCsParser.aspName);
        }

        this.aspxParser = aspxParser;
        this.aspxCsParser = aspxCsParser;
        this.aspxRoot = aspxRoot;

        this.urlPath = calculateUrlPath();
        this.filePath = calculateFilePath();

        collectParameters();

        setHttpMethod();
    }

    private boolean checkArguments(String aspName, String aspCsName) {

        String shortName = aspName.endsWith(".aspx") ? aspName.substring(0, aspName.length() - 5) : aspName;

        boolean aspxCsMatch = aspCsName.endsWith(".aspx.cs") &&
                shortName.equals(aspCsName.substring(0, aspCsName.indexOf(".aspx.cs")));

        boolean csMatch = aspCsName.endsWith(".cs") &&
                shortName.equals(aspCsName.substring(0, aspCsName.indexOf(".cs")));

        return aspxCsMatch || csMatch;
    }

    private void setHttpMethod() {
        httpMethods = map.size() == 0 ? set("GET") : set("GET", "POST");
    }

    private String calculateFilePath() {
        String aspxFilePath = aspxCsParser.file.getAbsolutePath();
        String aspxRootPath = aspxRoot.getAbsolutePath();

        return calculateRelativePath(aspxFilePath, aspxRootPath);
    }

    protected String calculateUrlPath() {
        String aspxFilePath = aspxParser.file.getAbsolutePath();
        String aspxRootPath = aspxRoot.getAbsolutePath();

        return calculateRelativePath(aspxFilePath, aspxRootPath);
    }

    final protected String calculateRelativePath(String aspxFilePath, String aspxRootPath) {
        if (aspxFilePath.startsWith(aspxRootPath)) {
            return aspxFilePath.substring(aspxRootPath.length());
        } else {
            String error = "AspxFilePath didn't start with aspxRoot : " +
                    aspxFilePath +
                    " didn't start with " +
                    aspxRootPath;
            LOG.error(error);
            assert false : error;
            return aspxParser.aspName;
        }
    }

    // TODO split this up
    private void collectParameters() {

        // reverse map to get parameter -> line numbers map
        for (Map.Entry<Integer, Set<String>> entry : aspxCsParser.lineNumberToParametersMap.entrySet()) {
            for (String key : entry.getValue()) {
                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<Integer>());
                }

                map.get(key).add(entry.getKey());
            }
        }

        // add entry for aspx parser's autogen'ed parameters, but only if we don't have a corresponding value from normal parsing
        for (String parameter : aspxParser.parameters) {
            boolean foundNormalParameter = false;

            for (String key : map.keySet()) {
                // these are known simple parameters; no generated names.
                if (parameter.endsWith("$" + key)) {
                    foundNormalParameter = true;
                    break;
                }
            }

            if (!foundNormalParameter) {
                map.put(parameter, list(0));
            }
        }

        for (List<Integer> integers : map.values()) {
            Collections.sort(integers);
        }
    }

    @Override
    public int compareRelevance(String checkedPath) {
        if (checkedPath.startsWith(urlPath)) {
            return urlPath.length() + 100 * urlPath.split("/").length;
        }

        String thisUrl = urlPath;
        if (checkedPath.startsWith("/")) {
            checkedPath = checkedPath.substring(1);
        }

        if (thisUrl.startsWith("/")) {
            thisUrl = thisUrl.substring(1);
        }

        String[] checkedPathParts = checkedPath.split("/");
        String[] thisUrlParts = thisUrl.split("/");

        int numMatchedParts = 0;
        int relevance = 0;

        for (int i = 0; i < checkedPathParts.length && i < thisUrlParts.length; i++) {

            String currentCheckedPart = checkedPathParts[i];
            String currentThisPart = thisUrlParts[i];

            if (currentCheckedPart.equalsIgnoreCase(currentThisPart)) {
                relevance += currentCheckedPart.length();
                numMatchedParts++;
            } else if (currentCheckedPart.startsWith("{") && currentCheckedPart.endsWith("}")) {
                relevance += currentThisPart.length();
                numMatchedParts++;
            }
        }

        if (relevance == 0) {
            relevance = compareWeakRelevance(checkedPath);
        }

        return relevance + numMatchedParts * 100;
    }

    //  Weak comparison by checking for urlPath anywhere in the endpoint. Used for matching
    //      against parametric URLs.
    //  This is a very SIMPLISTIC approach that should be replaced with something more reliable,
    //      or at least use some sort of threshold value
    int compareWeakRelevance(String checkedPath) {
        String thisRelativeUrl = urlPath;
        if (thisRelativeUrl.startsWith("/")) {
            thisRelativeUrl = thisRelativeUrl.substring(1);
        }

        if (thisRelativeUrl.endsWith("/")) {
            thisRelativeUrl = thisRelativeUrl.substring(0, thisRelativeUrl.length() - 1);
        }

        if (checkedPath.toLowerCase().contains(thisRelativeUrl.toLowerCase())) {
            return thisRelativeUrl.length() / 2;
        } else {
            return 0;
        }
    }

    @Nonnull
    @Override
    final protected List<String> getLintLine() {
        return list(getCSVLine(PrintFormat.DYNAMIC));
    }

    @Nonnull
    @Override
    final public Set<String> getParameters() {
        return map.keySet();
    }

    @Nonnull
    @Override
    final public Set<String> getHttpMethods() {
        return httpMethods;
    }

    @Nonnull
    @Override
    final public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    final public String getFilePath() {
        return filePath;
    }

    @Override
    final public int getStartingLineNumber() {
        return 0;
    }

    @Override
    final public int getLineNumberForParameter(String parameter) {
        return map.containsKey(parameter) ? map.get(parameter).get(0) : -1;
    }

    @Override
    final public boolean matchesLineNumber(int lineNumber) {
        return true;
    }
}
