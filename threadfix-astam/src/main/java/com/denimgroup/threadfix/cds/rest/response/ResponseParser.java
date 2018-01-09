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

package com.denimgroup.threadfix.cds.rest.response;


import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by amohammed on 6/12/2017.
 */

public class ResponseParser {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(ResponseParser.class);

    public static <T> RestResponse<T> getRestResponse(@Nullable InputStream responseStream, @Nonnull int responseCode, @Nullable String location){
        RestResponse<T> response = new RestResponse<T>();

        if(responseStream != null){
            try {
                response.data = IOUtils.toByteArray(responseStream);
            } catch (IOException e) {
                LOGGER.error("Unable to parse response stream due to IOException", e);
            }

        }

        if(location != null && !location.isEmpty() && responseCode == 201){
            response.uuid = extractNewUUID(location);
        }

        response.responseCode = responseCode;

        if(responseCode >= 200 && responseCode < 300){
            response.success = true;
        }

        LOGGER.debug("Setting response code to " + responseCode + ".");
        return response;
    }

    private static String extractNewUUID(String locationHeader) {
        String[] strings = locationHeader.split("/");
        String stringUUID = strings[strings.length - 1];
        return stringUUID;
    }

    public static <T> RestResponse<T> getErrorResponse(String errorText, int responseCode) {
        RestResponse<T> instance = new RestResponse<T>();

        instance.message = errorText;
        instance.responseCode = responseCode;
        instance.success = false;

        return instance;
    }

}
