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

package com.denimgroup.threadfix.mapper;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.Organization;
import com.secdec.astam.common.data.models.Appmgmt.ApplicationRegistration;

/**
 * Created by amohammed on 7/18/2017.
 */
public class ThreadfixApplicationMapper {

    public ThreadfixApplicationMapper(){}

    //TODO: validate incoming data
    public Application createApplication(ApplicationRegistration appRegistration){
        Application app = new Application();
        app.setUuid(appRegistration.getId().getValue());
        app.setName(appRegistration.getName());
        app.setUrl(appRegistration.getInfoUrl().getValue());
        app.setTeam(createOrganization(appRegistration));

        ApplicationRegistration.VersionControlRepository repo = appRegistration.getRepository();
        String repoUrl = repo.getRepositoryUrl().getValue();
        if(repoUrl != null){
            app.setRepositoryUrl(repoUrl);
        }

        String repoUser = repo.getUsername();
        if(repoUser != null){
            app.setRepositoryUserName(repoUser);
        }

        String repoToken = repo.getToken();
        if(repoToken != null){
            app.setRepositoryPassword(repoToken);
        }

        return app;
    }

    private Organization createOrganization(ApplicationRegistration appRegistration){
        Organization organization = new Organization();
        organization.setName(appRegistration.getOrganization());
        organization.setActive(true);
        return organization;
    }
}
