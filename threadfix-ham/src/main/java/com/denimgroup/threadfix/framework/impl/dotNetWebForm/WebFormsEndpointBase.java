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
package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.data.entities.ExplicitEndpointPathNode;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.*;

/*
 * Represents endpoints without any resource name, such as /some/path/.
 */
abstract class WebFormsEndpointBase extends AbstractEndpoint {

    private static final SanitizedLogger LOG = new SanitizedLogger(WebFormsEndpointBase.class);

    String      aspxFilePath;
    String      aspxCsFilePath;
    String      projectRoot;
    String      solutionRoot;
    String      aspxRoot;
    String      urlPath;
    String      filePath;

    int         startLine;
    int         endLine;

    Map<String, List<Integer>> map = map();
    protected String httpMethod;
    protected Map<String, RouteParameter> params = map();

    protected WebFormsEndpointBase() {

    }

    public WebFormsEndpointBase(File solutionRoot, File projectRoot, File aspxRoot, AspxParser aspxParser, AspxCsParser aspxCsParser) {
        if (!checkArguments(aspxParser.aspName, aspxCsParser.aspName)) {
            throw new IllegalArgumentException("Invalid aspx mappings pairs passed to WebFormsEndpointBase constructor: " +
                    aspxParser.aspName + " and " + aspxCsParser.aspName);
        }

        this.aspxFilePath = FilePathUtils.normalizePath(aspxParser.file.getAbsolutePath());
        this.aspxCsFilePath = FilePathUtils.normalizePath(aspxCsParser.file.getAbsolutePath());
        this.projectRoot = FilePathUtils.normalizePath(projectRoot.getAbsolutePath());
        this.solutionRoot = FilePathUtils.normalizePath(solutionRoot.getAbsolutePath());
        this.aspxRoot = FilePathUtils.normalizePath(aspxRoot.getAbsolutePath());

        this.urlPath = calculateUrlPath();
        this.filePath = calculateFilePath();

        this.urlPath = this.urlPath.replaceAll("\\\\", "/");

        collectParameters(aspxParser, aspxCsParser);

        //  It's difficult to discern which lines correspond to which endpoints
        //  (and endpoint responses can span multiple methods), giving
        //  the whole line range of the file is the closest we can get right now
        int numLines = CodeParseUtil.countLines(aspxCsParser.file.getAbsolutePath());
        this.startLine = 1;
        this.endLine = numLines;

        setHttpMethod("GET");
    }

    private boolean checkArguments(String aspName, String aspCsName) {

        String shortName = aspName.endsWith(".aspx") ? aspName.substring(0, aspName.length() - 5) : aspName;

        boolean aspxCsMatch = aspCsName.endsWith(".aspx.cs") &&
                shortName.equals(aspCsName.substring(0, aspCsName.indexOf(".aspx.cs")));

        boolean csMatch = aspCsName.endsWith(".cs") &&
                shortName.equals(aspCsName.substring(0, aspCsName.indexOf(".cs")));

        return aspxCsMatch || csMatch;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    private String calculateFilePath() {
        return calculateRelativePath(this.aspxCsFilePath, this.solutionRoot);
    }

    protected String calculateUrlPath() {
        return calculateRelativePath(this.aspxFilePath, this.aspxRoot);
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
            return null;
        }
    }

    // TODO split this up
    private void collectParameters(AspxParser aspxParser, AspxCsParser aspxCsParser) {

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

        for (String paramName : map.keySet()) {
            RouteParameter param = new RouteParameter(paramName);
            param.setParamType(RouteParameterType.FORM_DATA);
            params.put(paramName, param);
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
            } else {
                return 0;
            }
        }

        return relevance + numMatchedParts * 100;
    }

    @Override
    public boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness) {
        boolean isGenerallyRelevant = compareRelevance(endpoint) >= 0;
        if (strictness == EndpointRelevanceStrictness.LOOSE) {
            return isGenerallyRelevant;
        } else if (!isGenerallyRelevant) {
            return false;
        }

        String thisEndpoint = CodeParseUtil.trim(urlPath, "/");
        endpoint = CodeParseUtil.trim(endpoint, "/");

        if (thisEndpoint.equalsIgnoreCase(endpoint)) {
            return true;
        }

        String[] thisEndpointParts = StringUtils.split(thisEndpoint, '/');
        String[] endpointParts = StringUtils.split(endpoint, '/');

        if (thisEndpointParts.length != endpointParts.length) {
            return false;
        }

        for (int i = 0; i < thisEndpointParts.length; i++) {
            String thisPart = thisEndpointParts[i];
            String part = endpointParts[i];

            if (!thisPart.equalsIgnoreCase(part) && !thisPart.contains("{")) {
                return false;
            }
        }

        return true;

    }

    public abstract WebFormsEndpointBase duplicate();

    protected void copyPropertiesTo(WebFormsEndpointBase target) {
        target.aspxFilePath = this.aspxFilePath;
        target.aspxCsFilePath = this.aspxCsFilePath;
        target.projectRoot = this.projectRoot;
        target.aspxRoot = this.aspxRoot;
        target.urlPath = this.urlPath;
        target.filePath = this.filePath;

        target.startLine = this.startLine;
        target.endLine = this.endLine;

        target.httpMethod = this.httpMethod;

        target.params.putAll(this.params);
        target.map.putAll(this.map);
    }

    @Nonnull
    @Override
    final protected List<String> getLintLine() {
        return list(getCSVLine(PrintFormat.DYNAMIC));
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return params;
    }

    @Nonnull
    @Override
    final public String getHttpMethod() {
        return httpMethod;
    }

    @Nonnull
    @Override
    final public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {

        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();

        String[] pathParts = StringUtils.split(urlPath, '/');
        for (String part : pathParts) {
            result.add(new ExplicitEndpointPathNode(part));
        }

        return result;

    }

    @Nonnull
    @Override
    final public String getFilePath() {
        return filePath;
    }

    @Override
    final public int getStartingLineNumber() {
        return this.startLine;
    }

    @Override
    public int getEndingLineNumber() {
        return this.endLine;
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
