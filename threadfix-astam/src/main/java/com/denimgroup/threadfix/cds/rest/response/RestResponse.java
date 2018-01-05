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
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.cds.rest.response;

import com.denimgroup.threadfix.util.Result;

/**
 * This is the basic RestResponse which is returned by all the methods on the ThreadFix server side.
 */
public class RestResponse<T> {

    public String message = "";
    public boolean success = false;
    public int responseCode = -1;
    public T object = null;
    public byte[] data;
    public String uuid = null;

    public static <T> RestResponse<T> failure(String response) {
        RestResponse<T> restResponse = new RestResponse<T>();
        restResponse.message = response;
        return restResponse;
    }

    public static <T> RestResponse<T> success(T object) {
        RestResponse<T> restResponse = new RestResponse<T>();
        restResponse.success = true;
        restResponse.object  = object;
        return restResponse;
    }

    public static <T> RestResponse<T> resultError(Result result) {
        return failure(result.getErrorMessage());
    }

    public T getObject(){
        if (object != null && success){
            return object;
        } else return null;
    }
}
