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

package com.denimgroup.threadfix.cds.rest;

import com.denimgroup.threadfix.cds.rest.response.RestResponse;

import javax.annotation.Nonnull;

/**
 * Created by amohammed on 7/31/2017.
 */
public interface HttpMethods {
    <T> RestResponse<T> httpGet(@Nonnull String path);

    <T> RestResponse<T> httpGet(@Nonnull String path, @Nonnull String param);

    <T> RestResponse<T> httpPost(@Nonnull String path, byte[] entity);

    <T> RestResponse<T> httpPut(@Nonnull String path, @Nonnull String param, byte[] bytes);

    <T> RestResponse<T> httpDelete(@Nonnull String path, @Nonnull String param);
}
