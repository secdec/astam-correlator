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
import com.secdec.astam.common.data.models.Appmgmt.*;

/**
 * Created by amohammed on 6/23/2017.
 */
public interface AstamApplicationClient  {

    //application/
    RestResponse<ApplicationRegistrationSet> getAllAppRegistrations();
    RestResponse createAppRegistration(ApplicationRegistration appRegistration);

    //application/{applicationRegistrationId}
    RestResponse<ApplicationRegistration> getAppRegistration(String appRegistrationId);
    RestResponse<ApplicationRegistration> updateAppRegistration(String appRegistrationId, ApplicationRegistration appRegistration);
    RestResponse<ApplicationRegistration> deleteAppRegistration(String appRegistrationId);

    //application/version
    RestResponse<ApplicationVersionSet> getAllAppVersions(String appRegistrationId);
    RestResponse createAppVersion(ApplicationVersion appVersion);

    //application/version/{versionId}
    RestResponse<ApplicationVersion> getAppVersion(String versionId);
    RestResponse updateAppVersion(String versionId, ApplicationVersion applicationVersion);
    RestResponse deleteAppVersion(String versionId);

    //application/environment
    RestResponse<ApplicationEnvironmentSet> getAllAppEnvironments();
    RestResponse createEnvironment(ApplicationEnvironment applicationEnvironment);

    //application/environment/{environmentId}
    RestResponse<ApplicationEnvironment> getEnvironment(String environmentId);
    RestResponse updateEnvironment(String environmentId, ApplicationEnvironment applicationEnvironment);
    RestResponse deleteEnvironment(String environmentId);

    //application/deployment
    RestResponse<ApplicationDeploymentSet> getAllAppDeployments();
    RestResponse createAppDeployment(ApplicationDeployment applicationDeployment);

    //application/deployment/{deploymentId}
    RestResponse<ApplicationDeployment> getAppDeployment(String deploymentId);
    RestResponse updateAppDeployment(String deploymentId, ApplicationDeployment applicationDeployment);
    RestResponse deleteAppDeployment(String deploymentId);
}
