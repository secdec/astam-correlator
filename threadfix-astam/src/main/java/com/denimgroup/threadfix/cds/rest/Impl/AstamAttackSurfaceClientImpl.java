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

package com.denimgroup.threadfix.cds.rest.Impl;

import com.denimgroup.threadfix.cds.rest.AstamAttackSurfaceClient;
import com.denimgroup.threadfix.cds.rest.HttpMethods;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.secdec.astam.common.data.models.Attacksurface.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;



/**
 * Created by amohammed on 6/23/2017.
 */

@Component
public class AstamAttackSurfaceClientImpl implements AstamAttackSurfaceClient {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamAttackSurfaceClientImpl.class);

    @Autowired
    private HttpMethods httpUtils;

    private final static String CONTROLLER_ATK_SUR = "AttackSurface/",
            RAW_DISCOVERED_ATTACK_SURFACE = "RawDiscoveredAttackSurface/",
            ENTRY_POINT = "EntryPoint/",
            WEB = "web/",
            MOBILE = "mobile/",
            EXCEPTION_MESSAGE = "InvalidProtocolBufferException while attempting to parse retrieved protobuf data.";

    public AstamAttackSurfaceClientImpl( ){
    }

    @Override
    public RestResponse<RawDiscoveredAttackSurfaceSet> getAllRawDiscoveredAttackSurfaces() {
        RestResponse<RawDiscoveredAttackSurfaceSet> response = httpUtils.httpGet(
                CONTROLLER_ATK_SUR + RAW_DISCOVERED_ATTACK_SURFACE );
        if(response.success){
            try {
                response.object = RawDiscoveredAttackSurfaceSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createRawDiscoveredAttackSurface(@Nonnull RawDiscoveredAttackSurface rawDiscoveredAttackSurface) {
        byte[] entity = rawDiscoveredAttackSurface.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_ATK_SUR + RAW_DISCOVERED_ATTACK_SURFACE, entity);
        return response;
    }

    @Override
    public RestResponse<RawDiscoveredAttackSurface> getRawDiscoveredAttackSurface(
            @Nonnull String rawDiscoveredAttackSurfaceId) {
        RestResponse<RawDiscoveredAttackSurface> response = httpUtils.httpGet(
                CONTROLLER_ATK_SUR
                        + RAW_DISCOVERED_ATTACK_SURFACE
                        + rawDiscoveredAttackSurfaceId);
        if(response.success){
            try {
                response.object = RawDiscoveredAttackSurface.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateRawDiscoveredAttackSurface(
            @Nonnull String rawDiscoveredAttackSurfaceId,
            @Nonnull RawDiscoveredAttackSurface rawDiscoveredAttackSurface) {

        byte[] entity = rawDiscoveredAttackSurface.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_ATK_SUR + RAW_DISCOVERED_ATTACK_SURFACE,
                rawDiscoveredAttackSurfaceId, entity);
        return response;
    }

    @Override
    public RestResponse deleteRawDiscoveredAttackSurface(@Nonnull String rawDiscoveredAttackSurfaceId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_ATK_SUR + RAW_DISCOVERED_ATTACK_SURFACE,
                rawDiscoveredAttackSurfaceId);
        return response;
    }

    @Override
    public RestResponse<EntryPointWebSet> getAllEntryPointsWeb() {
        RestResponse<EntryPointWebSet> response = httpUtils.httpGet(CONTROLLER_ATK_SUR + ENTRY_POINT + WEB);
        if(response.success){
            try {
                response.object = EntryPointWebSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createEntryPointWeb(@Nonnull EntryPointWeb entryPointWeb) {
        byte[] entity = entryPointWeb.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_ATK_SUR + ENTRY_POINT + WEB, entity);
        return response;
    }

    @Override
    public RestResponse<EntryPointWeb> getEntryPointWeb(@Nonnull String entryPointWebId) {
        RestResponse<EntryPointWeb> response = httpUtils.httpGet(CONTROLLER_ATK_SUR + ENTRY_POINT + WEB,
                entryPointWebId);
        try {
            response.object = EntryPointWeb.parseFrom(response.data);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error(EXCEPTION_MESSAGE, e);
        }
        return response;
    }

    @Override
    public RestResponse updateEntryPointWeb(@Nonnull String entryPointWebId,
                                            @Nonnull EntryPointWeb entryPointWeb) {
        byte[] entity = entryPointWeb.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_ATK_SUR + ENTRY_POINT + WEB,
                entryPointWebId, entity);
        return response;
    }

    @Override
    public RestResponse deleteEntryPointWeb(@Nonnull String entryPointWebId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_ATK_SUR + ENTRY_POINT + WEB, entryPointWebId);
        return response;
    }

    @Override
    public RestResponse<EntryPointMobileSet> getAllEntryMobilePoints() {
        RestResponse<EntryPointMobileSet> response = httpUtils.httpGet(CONTROLLER_ATK_SUR + ENTRY_POINT + MOBILE);
        if(response.success){
            try {
                response.object = EntryPointMobileSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createEntryMobilePoint(@Nonnull EntryPointMobile entryPointMobile) {
        byte[] entity = entryPointMobile.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_ATK_SUR + ENTRY_POINT + MOBILE, entity);
        return response;
    }

    @Override
    public RestResponse<EntryPointMobile> getEntryPointMobile(@Nonnull String entryPointMobileId) {
        RestResponse<EntryPointMobile> response = httpUtils.httpGet(CONTROLLER_ATK_SUR + ENTRY_POINT + MOBILE,
                entryPointMobileId);
        if(response.success){
            try {
                response.object = EntryPointMobile.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateEntryPointMobile(@Nonnull String entryPointMobileId,
                                               @Nonnull EntryPointMobile entryPointMobile) {
        byte[] entity = entryPointMobile.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_ATK_SUR + ENTRY_POINT + MOBILE,
                entryPointMobileId, entity);
        return response;
    }

    @Override
    public RestResponse deleteEntryPointMobile(@Nonnull String entryPointMobileId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_ATK_SUR + ENTRY_POINT + MOBILE,
                entryPointMobileId);
        return response;
    }

}
