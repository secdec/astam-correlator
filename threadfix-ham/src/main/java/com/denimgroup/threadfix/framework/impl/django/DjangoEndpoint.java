// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.setFrom;

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
                    "ta|te|th|tr|tt|udm|uk|ur|vi|zh_Hans|zh_Hant";

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

    private Set<String> httpMethods;
    private Map<String, ParameterDataType> parameters;

    public DjangoEndpoint(String filePath, String urlPath,
                          Collection<String> httpMethods, Map<String, ParameterDataType> parameters,
                          boolean isInternationalized) {
        this.filePath = filePath;
        this.urlPath = urlPath;
        if (httpMethods != null)
            this.httpMethods = setFrom(httpMethods);
        if (parameters != null)
            this.parameters = parameters;

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

//        if (isInternationalized) {
//            pattern = DjangoPathUtil.combine("/(?:" + I18_LANG_PATTERN + ")/", pattern);
//            this.urlPath = DjangoPathUtil.combine("/(i18)", this.urlPath);
//        }

        urlPattern = Pattern.compile(pattern);
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

    @Nonnull
    @Override
    public Map<String, ParameterDataType> getParameters() {
        return parameters;
    }

    @Nonnull
    @Override
    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    @Nonnull
    @Override
    public String getUrlPath() {
        return urlPath;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public int getStartingLineNumber() {
        return 0;
    }

    @Override
    public int getLineNumberForParameter(String parameter) {
        return 0;
    }

    @Override
    public boolean matchesLineNumber(int lineNumber) {
        return false;
    }

    @Nonnull
    @Override
    protected List<String> getLintLine() {
        return null;
    }
}
