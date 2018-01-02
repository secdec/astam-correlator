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

package com.denimgroup.threadfix.mapper;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ApplicationVersion;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationDeployment;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationEnvironment;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Appmgmt;
import com.secdec.astam.common.data.models.Common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/12/2017.
 */
public class AstamApplicationMapper {
    private Appmgmt.ApplicationRegistration applicationRegistration;
    private Appmgmt.ApplicationVersion applicationVersion;
    private Appmgmt.ApplicationDeployment applicationDeployment;
    private Appmgmt.ApplicationEnvironment applicationEnvironment;


    public AstamApplicationMapper(){}

    public void setup(){}

    private Appmgmt.ApplicationRegistration.VersionControlRepository createVersionControlRepository(Application app) {
        Appmgmt.ApplicationRegistration.VersionControlRepository.Builder repoBuilder =
                Appmgmt.ApplicationRegistration.VersionControlRepository.newBuilder();

        String repoUrl = app.getRepositoryUrl();
        if (repoUrl != null) {
            repoBuilder.setRepositoryUrl(ProtobufMessageUtils.createUrl(repoUrl))
                .setRepositoryType(Appmgmt.ApplicationRegistration.VersionControlRepository.RepositoryType.GIT);
        }

        String repoUser = app.getRepositoryUserName();
        if (repoUser != null) {
            repoBuilder.setUsername(repoUser);
        }

        String repoToken = app.getRepositoryPassword();
        if (repoToken != null) {
            repoBuilder.setToken(repoToken);
        }


        return repoBuilder.build();
    }

    public void setApplication(Application app) {
        Organization organization = app.getOrganization();
        Appmgmt.ApplicationRegistration.VersionControlRepository repo = createVersionControlRepository(app);

        Appmgmt.ApplicationRegistration.Builder appBuilder = Appmgmt.ApplicationRegistration.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(app))
                .setName(app.getName())
                .setRecordData(ProtobufMessageUtils.createRecordData(app))
                .setOrganization(organization.getName())
                .setApplicationType(Appmgmt.ApplicationType.WEB)
                .setRepository(repo);


        String url = app.getUrl();
        if (url != null) {
            appBuilder.setInfoUrl(ProtobufMessageUtils.createUrl(url));
        }
        applicationRegistration = appBuilder.build();
    }

    //TODO: map this, also possible map/use SourceCodeStatus instead of ApplicationVersion
    //URL changeset_url = 7;
    //string build_id = 8;
    //URL build_url = 9;
    public void setApplicationVersion(ApplicationVersion appVersion){
        Common.UUID appRegistrationUuid = ProtobufMessageUtils.createUUID(appVersion.getApplication());

        Appmgmt.ApplicationVersion applicationVersion = Appmgmt.ApplicationVersion.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(appVersion))
                .setRecordData(ProtobufMessageUtils.createRecordData(appVersion))
                .setApplicationRegistrationId(appRegistrationUuid)
                .setTime(ProtobufMessageUtils.createTimestamp(appVersion.getDate()))
                .build();

        this.applicationVersion = applicationVersion;
    }

   public void setApplicationEnvironment(AstamApplicationEnvironment appEnvironment) {
        Appmgmt.ApplicationEnvironment applicationEnvironment = Appmgmt.ApplicationEnvironment.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(appEnvironment))
                .setRecordData(ProtobufMessageUtils.createRecordData(appEnvironment))
                .setName(appEnvironment.getName()).build();

        this.applicationEnvironment = applicationEnvironment;
    }

    public void setApplicationDeployment(AstamApplicationDeployment appDeployment,
                                         ApplicationVersion appVersion,
                                         AstamApplicationEnvironment appEnvironment){

        Appmgmt.ApplicationDeployment applicationDeployment = Appmgmt.ApplicationDeployment.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(appDeployment))
                .setRecordData(ProtobufMessageUtils.createRecordData(appDeployment))
                // make sure to call setApplicationEnvironment & Version prior
                .setApplicationEnvironmentId(ProtobufMessageUtils.createUUID(appEnvironment))
                .setApplicationVersionId(ProtobufMessageUtils.createUUID(appVersion))
                .setName(appDeployment.getName()).build();

     this.applicationDeployment = applicationDeployment;
    }

    public void writeApplicationToOutput(OutputStream outputStream) throws IOException {
        applicationRegistration.writeTo(outputStream);
    }

    public Appmgmt.ApplicationRegistration getAppRegistration(){return applicationRegistration;}

    public Appmgmt.ApplicationEnvironment getAppEnvironment() { return applicationEnvironment;}

    public Appmgmt.ApplicationVersion getAppVersion(){ return applicationVersion;}

    public Appmgmt.ApplicationDeployment getAppDeployment(){ return applicationDeployment;}
}