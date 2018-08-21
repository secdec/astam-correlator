////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 4/25/2017.
 */
public class DjangoEndpoint extends AbstractEndpoint {

    // https://github.com/django/django/tree/master/django/conf/locale
    private static String I18_LANG_PATTERN =
                    "af|ar|ast|bg|bn|br|bs|ca|cs|cy|da|de|de_CH|dsb|el|en"  + "|" +
                    "en_AU|en_GB|eo|es|es_AR|es_CO|es_MX|es_NI|es_PR|es_VE" + "|" +
                    "et|eu|fa|fi|fr|fy|ga|gd|gl|he|hi|hr|hsb|hu|ia|id|io"   + "|" +
                    "is|it|ja|ka|kk|km|kn|ko|lb|lt|lv|mk|ml|mn|mr|my|ne|nl" + "|" +
                    "nn|os|pa|pl|pt|pt_BR|ro|ru|sk|sl|sq|sr|sr_Latn|sv|sw"  + "|" +
                    "ta|te|th|tr|tt|udm|uk|ur|vi|zh_Hans|zh_Hant"; // for use in regex

    private final static List<String> I18_SUPPORTED_LANGS = list(
            "af", "ar", "ast", "bg", "bn", "br", "bs", "ca", "cs", "cy", "da", "de", "de_CH", "dsb",
            "el", "en", "en_AU", "en_GB", "eo", "es", "es_AR", "es_CO", "es_MX", "es_NI", "es_PR", "es_VE",
            "et", "eu", "fa", "fi", "fr", "fy", "ga", "gd", "gl", "he", "hi", "hr", "hsb", "hu", "ia", "id",
            "io", "is", "it", "ja", "ka", "kk", "km", "kn", "ko", "lb", "lt", "lv", "mk", "ml", "mn", "mr",
            "my", "ne", "nl", "nn", "os", "pa", "pl", "pt", "pt_BR", "ro", "ru", "sk", "sl", "sq", "sr",
            "sr_Latn", "sv", "sw", "ta", "te", "th", "tr", "tt", "udm", "uk", "ur", "vi", "zh_Hans", "zh_Hant");

    private String filePath;
    private String urlPath;
    private Pattern urlPattern;
    private boolean isInternationalized;
    private int startLineNumber = -1;
    private int endLineNumber = -1;

    private String httpMethod;
    private Map<String, RouteParameter> parameters;

    private DjangoEndpoint() {

    }

    public DjangoEndpoint(String filePath, String urlPath,
                          @Nonnull String httpMethod,
                          Map<String, RouteParameter> parameters,
                          boolean isInternationalized) {

        this.filePath = FilePathUtils.normalizePath(filePath);
        this.urlPath = urlPath;
        this.httpMethod = httpMethod;
        if (parameters != null)
            this.parameters = parameters;

        this.urlPath = this.urlPath.replace('\\', '/');

        if (!this.urlPath.startsWith("/")) {
            this.urlPath = "/" + urlPath;
        }

        //  Remove named groups, not supported by Java 6
        String pattern = urlPath;
        if (pattern.contains("(?P<")) {
            pattern = pattern.replaceAll("\\(\\?P<\\w+>", "(");
        }

        //  Django regexes like to have multiple start-string '^' symbols, remove all of them except at the beginning
        //  of the string (if any)
        if (pattern.contains("^")) {
            boolean startsWithUpper = pattern.charAt(0) == '^';
            pattern = pattern.replaceAll("\\^", "");
            if (startsWithUpper) {
                pattern = "^" + pattern;
            }
        }
        if (pattern.contains("$")) {
            boolean endsWithDollar = pattern.charAt(pattern.length() - 1) == '$';
            pattern = pattern.replaceAll("\\$", "");
            if (endsWithDollar) {
                pattern += "$";
            }
        }

        this.isInternationalized = isInternationalized;

        urlPattern = Pattern.compile(pattern);

    }

    public void setLineNumbers(int startLine, int endLine) {
        startLineNumber = startLine;
        endLineNumber = endLine;
    }

    @Override
    public int compareRelevance(String endpoint) {

        if (isInternationalized) {
            if (endpoint.startsWith("/")) {
                endpoint = endpoint.substring(1);
            }

            String[] endpointParts;
            endpointParts = endpoint.split("/");

            if (endpointParts.length == 0) {
                return -1;
            } else if (!I18_SUPPORTED_LANGS.contains(endpointParts[0])) {
                return -1;
            } else {
                //  The first part was a supported language code (ie 'en', 'fr'), remove that part of
                //  the path so that it doesn't interfere with pattern matching
                endpoint = endpoint.substring(endpoint.indexOf('/'));
                // /en/some-page -> /some-page
            }
        }

        if (endpoint.equalsIgnoreCase(urlPath)) {
            return 100;
        } else if (urlPattern.matcher(endpoint).matches()) {
            return urlPath.length();
        } else {
            return -1;
        }
    }

    @Override
    public boolean isRelevant(String endpoint, EndpointRelevanceStrictness strictness) {
        boolean generallyRelevant = compareRelevance(endpoint) >= 0;
        if (strictness == EndpointRelevanceStrictness.LOOSE) {
            return generallyRelevant;
        } else if (!generallyRelevant) {
            return false;
        }

        if (isInternationalized) {
            //  The initial compareRelevance showed that the endpoint at least contains
            //  proper internationalization, so we just need to strip the language
            //  code for endpoint comparison
            if (endpoint.startsWith("/")) {
                endpoint = endpoint.substring(1);
            }

            //  The first part was a supported language code (ie 'en', 'fr'), remove that part of
            //  the path so that it doesn't interfere with pattern matching
            endpoint = endpoint.substring(endpoint.indexOf('/'));
        }

        if (endpoint.equalsIgnoreCase(urlPath)) {
            return true;
        }

        String strippedEndpoint = endpoint.replaceFirst(urlPattern.pattern(), "");
        strippedEndpoint = CodeParseUtil.trim(strippedEndpoint, "/");

        //  If this endpoint is a perfect match then there shouldn't be anything after replacing
        //  it in the URL
        return strippedEndpoint.length() == 0;
    }

    @Nonnull
    @Override
    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        if (!isInternationalized) {
            return urlPath;
        } else {
            return "/^(?P<i18>[\\w\\-_]+)" + urlPath;
        }
    }

    @Nonnull
    @Override
    public List<EndpointPathNode> getUrlPathNodes() {
        List<EndpointPathNode> result = new ArrayList<EndpointPathNode>();
        String[] pathParts = StringUtils.split(urlPath);

        if (isInternationalized) {
            result.add(new MultiValueEndpointPathNode(I18_SUPPORTED_LANGS));
        }

        for (String part : pathParts) {
            if (part.startsWith("^")) {
                part = part.substring(1);
            }
            if (part.endsWith("$")) {
                part = part.substring(0, part.length() - 1);
            }
            if (part.isEmpty()) {
                continue;
            }
            if (part.contains("(") || part.contains("[")) {
                result.add(new WildcardEndpointPathNode(part));
            } else {
                result.add(new ExplicitEndpointPathNode(part));
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(@Nonnull String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return startLineNumber;
    }

    @Override
    public int getEndingLineNumber() {
        return endLineNumber;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return 0;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        if (startLineNumber < 0) {
            return false;
        } else if (endLineNumber < 0) {
            return lineNumber == startLineNumber;
        } else {
            return lineNumber >= startLineNumber && lineNumber <= endLineNumber;
        }
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }

    @Nonnull
    @Override
    public String toString() {
        return
            urlPath +
            " - " +
            (filePath == null ? "<no source file>" : filePath) +
            ":" +
            startLineNumber +
            "," +
            endLineNumber;
    }
}
