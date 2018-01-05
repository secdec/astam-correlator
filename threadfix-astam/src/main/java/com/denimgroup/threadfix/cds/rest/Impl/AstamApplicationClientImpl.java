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

import com.denimgroup.threadfix.cds.rest.AstamApplicationClient;
import com.denimgroup.threadfix.cds.rest.HttpMethods;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.secdec.astam.common.data.models.Appmgmt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Created by amohammed on 6/23/2017.
 */

@Component
public class AstamApplicationClientImpl  implements AstamApplicationClient {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamApplicationClientImpl.class);

    @Autowired
    private HttpMethods httpUtils;

    private final static String CONTROLLER_APP = "application/",
            REGISTRATION = "registration/",
            VERSION = "version/",
            ENVIRONMENT = "environment/",
            DEPLOYMENT = "deployment/",
            EXCEPTION_MESSAGE = "InvalidProtocolBufferException while attempting to parse retrieved protobuf data.";

    public AstamApplicationClientImpl(){

    }

    @Override
    public RestResponse<ApplicationRegistrationSet> getAllAppRegistrations() {
        RestResponse<ApplicationRegistrationSet> response = httpUtils.httpGet(CONTROLLER_APP + REGISTRATION);
        if(response.success){
            try {
                response.object = ApplicationRegistrationSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createAppRegistration(@Nonnull ApplicationRegistration applicationRegistration) {
        byte[] entity = applicationRegistration.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_APP + REGISTRATION, entity);
        return response;
    }

    @Override
    public RestResponse<ApplicationRegistration> getAppRegistration(@Nonnull String appRegistrationId) {
        RestResponse response = httpUtils.httpGet(CONTROLLER_APP + REGISTRATION, appRegistrationId);
        if(response.success){
            try {
                response.object = ApplicationRegistration.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }

        return response;
    }

    @Override
    public RestResponse<ApplicationRegistration> updateAppRegistration(@Nonnull String appRegistrationId,
                                                                       @Nonnull ApplicationRegistration applicationRegistration) {
        byte[] entity = applicationRegistration.toByteArray();
        RestResponse response = httpUtils.httpPut(
                CONTROLLER_APP + REGISTRATION, appRegistrationId , entity);
        return response;
    }

    @Override
    public RestResponse<ApplicationRegistration> deleteAppRegistration(@Nonnull String appRegistrationId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_APP + REGISTRATION, appRegistrationId);
        return response;
    }

    @Override
    public RestResponse<ApplicationVersionSet> getAllAppVersions(@Nonnull String appRegistrationId) {
        RestResponse<ApplicationVersionSet> response = httpUtils.httpGet(
                CONTROLLER_APP + VERSION, appRegistrationId);
        if(response.success){
            try {
                response.object = ApplicationVersionSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createAppVersion(@Nonnull ApplicationVersion applicationVersion) {
        byte[] entity = applicationVersion.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_APP + VERSION, entity);
        return response;
    }

    @Override
    public RestResponse<ApplicationVersion> getAppVersion(@Nonnull String versionId) {
        RestResponse<ApplicationVersion> response = httpUtils.httpGet(CONTROLLER_APP + VERSION, versionId);
        if(response.success){
            try {
                response.object = ApplicationVersion.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateAppVersion(@Nonnull String versionId, @Nonnull ApplicationVersion applicationVersion) {
        byte[] entity = applicationVersion.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_APP + VERSION, versionId, entity);
        return response;
    }

    @Override
    public RestResponse deleteAppVersion(@Nonnull String versionId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_APP + VERSION, versionId);
        return response;
    }

    @Override
    public RestResponse<ApplicationEnvironmentSet> getAllAppEnvironments() {
        RestResponse<ApplicationEnvironmentSet> response = httpUtils.httpGet(CONTROLLER_APP + ENVIRONMENT);
        if(response.success){
            try {
                response.object = ApplicationEnvironmentSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createEnvironment(@Nonnull ApplicationEnvironment applicationEnvironment) {
        byte[] entity = applicationEnvironment.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_APP + ENVIRONMENT, entity);
        return response;
    }

    @Override
    public RestResponse<ApplicationEnvironment> getEnvironment(@Nonnull String environmentId) {
        RestResponse<ApplicationEnvironment> response = httpUtils.httpGet(
                CONTROLLER_APP + ENVIRONMENT, environmentId);
        if(response.success){
            try {
                response.object = ApplicationEnvironment.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }

        return response;
    }

    @Override
    public RestResponse updateEnvironment(@Nonnull String environmentId,
                                          @Nonnull ApplicationEnvironment applicationEnvironment) {
        byte[] entity = applicationEnvironment.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_APP + ENVIRONMENT, environmentId, entity);
        return response;
    }

    @Override
    public RestResponse deleteEnvironment(@Nonnull String environmentId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_APP + ENVIRONMENT, environmentId);
        return response;
    }

    @Override
    public RestResponse<ApplicationDeploymentSet> getAllAppDeployments() {
        RestResponse<ApplicationDeploymentSet> response = httpUtils.httpGet(CONTROLLER_APP + DEPLOYMENT);

        if(response.success){
            try {
                response.object = ApplicationDeploymentSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createAppDeployment(@Nonnull ApplicationDeployment applicationDeployment) {
        byte[] entity = applicationDeployment.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_APP + DEPLOYMENT , entity);
        return response;
    }

    @Override
    public RestResponse<ApplicationDeployment> getAppDeployment(@Nonnull String deploymentId) {
        RestResponse<ApplicationDeployment> response = httpUtils.httpGet(
                CONTROLLER_APP + DEPLOYMENT, deploymentId);
        if(response.success){
            try {
                response.object = ApplicationDeployment.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateAppDeployment(@Nonnull String deploymentId,
                                            @Nonnull ApplicationDeployment applicationDeployment) {
        byte[] entity = applicationDeployment.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_APP + DEPLOYMENT, deploymentId, entity);
        return response;
    }

    @Override
    public RestResponse deleteAppDeployment(@Nonnull String deploymentId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_APP + DEPLOYMENT, deploymentId);
        return response;
    }

}
