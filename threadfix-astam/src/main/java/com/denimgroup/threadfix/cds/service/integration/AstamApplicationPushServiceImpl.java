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

package com.denimgroup.threadfix.cds.service.integration;

import com.denimgroup.threadfix.cds.messaging.AstamMessageManager;
import com.denimgroup.threadfix.cds.rest.AstamApplicationClient;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.cds.service.AstamApplicationPushService;
import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Appmgmt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.data.enums.AstamEntityType.APP_REGISTRATION;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_CREATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_UPDATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.*;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType.DATA_SET_COMPLETE;

/**
 * Created by amohammed on 6/28/2017.
 */
@Service
public class AstamApplicationPushServiceImpl implements AstamApplicationPushService {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamApplicationPushServiceImpl.class);


    private AstamApplicationClient applicationClient;
    private AstamMessageManager messageNotifier;
    private UuidUpdater uuidUpdater;


    @Autowired
    public AstamApplicationPushServiceImpl(AstamApplicationClient applicationClient,
                                           AstamMessageManager messageManager,
                                           UuidUpdater uuidUpdater){

        this.applicationClient = applicationClient;
        this.messageNotifier = messageManager;
        this.uuidUpdater = uuidUpdater;
    }

    @Override
    public void pushApplicationToAstam(int appId, ApplicationRegistration appReg) {
        pushAppRegistration(appId, appReg);
    }

    @Override
    public void pushAppRegistration(int appId, ApplicationRegistration appRegistration){
        String appRegId = appRegistration.getId().getValue();
        RestResponse<ApplicationRegistration> response = applicationClient.getAppRegistration(appRegId);
        boolean doesExist = response.success && response.getObject() != null && response.getObject().hasId();
        pushAppRegistration(appId, appRegistration, doesExist);
    }

    @Override
    public boolean pushAppRegistration(int appId, ApplicationRegistration appRegistration, boolean doesExist){
        RestResponse<ApplicationRegistration> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = applicationClient.createAppRegistration(appRegistration);

            if (restResponse.success){
                success = true;
                uuidUpdater.updateUUID(appId, restResponse.uuid, APP_REGISTRATION);
            } else if(restResponse.responseCode == 409){
                pushAppRegistration(0 , appRegistration, true);
            }
        } else {
            restResponse = applicationClient.updateAppRegistration(appRegistration.getId().getValue(), appRegistration);

            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushAppRegistration(appId, appRegistration, false);
            }
        }
        return success;
    }

    @Override
    public void pushAppVersionSet(ApplicationVersionSet localAppVersionSet, String appRegistrationId){
        List<ApplicationVersion> localAppVersionList = localAppVersionSet.getApplicationVersionsList();
        ApplicationVersionSet cdsAppVersionSet = null;
        List<String> entityIds = new ArrayList();
        List<ApplicationVersion> cdsAppVersionList = null;
        boolean isCreateOperation = false;

        try {
            cdsAppVersionSet = applicationClient.getAllAppVersions(appRegistrationId).getObject();
        }catch (NullPointerException e){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(cdsAppVersionSet == null || cdsAppVersionList.isEmpty()){
            isCreateOperation = true;
        } else {
            cdsAppVersionList = cdsAppVersionSet.getApplicationVersionsList();
        }

        boolean doesExist;
        boolean success;

        for (ApplicationVersion localAppVersion: localAppVersionList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = cdsAppVersionList.contains(localAppVersion);
            }

            success = pushAppVersion(localAppVersion, doesExist);

            if(success) {
                entityIds.add(localAppVersion.getId().getValue());
            }
        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_APPLICATION_VERSION, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_APPLICATION_VERSION, DATA_UPDATE, DATA_SET_COMPLETE, entityIds );
        }
    }

    @Override
    public boolean pushAppVersion(ApplicationVersion appVersion, boolean doesExist){
        RestResponse<ApplicationVersion> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = applicationClient.createAppVersion(appVersion);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(appVersion.getId().getValue());
               // uuidUpdater.updateUUID(id, restResponse.uuid, APP_);
            } else if(restResponse.responseCode == 409){
                pushAppVersion(appVersion, true);
            }
        } else {
            restResponse = applicationClient.updateAppVersion(appVersion.getId().getValue(), appVersion);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushAppVersion(appVersion, false);
            }
        }

        return success;
    }

    @Override
    public void pushAppEnvironmentSet(ApplicationEnvironmentSet localAppEnvironmentSet){
        List<ApplicationEnvironment> localAppEnvironmentList = localAppEnvironmentSet.getApplicationEnvironmentsList();
        ApplicationEnvironmentSet cdsAppEnvironmentSet = null;
        List<ApplicationEnvironment> cdsAppEnvironmentList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            cdsAppEnvironmentSet = applicationClient.getAllAppEnvironments().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(cdsAppEnvironmentSet == null || cdsAppEnvironmentSet.getApplicationEnvironmentsList().isEmpty() ){
            isCreateOperation = true;
        } else {
            cdsAppEnvironmentList = cdsAppEnvironmentSet.getApplicationEnvironmentsList();
        }

        boolean doesExist;
        boolean success;

        for (ApplicationEnvironment localAppEnvironment: localAppEnvironmentList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = cdsAppEnvironmentList.contains(localAppEnvironment);
            }

            success = pushAppEnvironment(localAppEnvironment, doesExist);

            if(success) {
                entityIds.add(localAppEnvironment.getId().getValue());
            }
        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_APPLICATION_ENVIRONMENT, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_APPLICATION_ENVIRONMENT, DATA_UPDATE, DATA_SET_COMPLETE, entityIds );
        }


    }

    @Override
    public boolean pushAppEnvironment(ApplicationEnvironment appEnvironment, boolean doesExist){
        RestResponse<ApplicationEnvironment> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = applicationClient.createEnvironment(appEnvironment);
            if (restResponse.success){
                success = true;
            } else if(restResponse.responseCode == 409){
                pushAppEnvironment(appEnvironment, true);
            }
        } else {
            restResponse = applicationClient.updateEnvironment(appEnvironment.getId().getValue(), appEnvironment);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushAppEnvironment(appEnvironment, false);
            }
        }

        return success;
    }

    @Override
    public void pushAppDeploymentSet(ApplicationDeploymentSet localAppDeploymentSet){
        List<ApplicationDeployment> localAppDeploymentList = localAppDeploymentSet.getApplicationDeploymentsList();
        ApplicationDeploymentSet cdsAppDeploymentSet = null;
        List<ApplicationDeployment> cdsAppDeploymentsList = null;
        List<String> entityIds = new ArrayList();
        boolean isCreateOperation = false;

        try {
            cdsAppDeploymentSet = applicationClient.getAllAppDeployments().getObject();
        }catch (NullPointerException e){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(cdsAppDeploymentSet == null || cdsAppDeploymentsList.isEmpty()) {
            isCreateOperation = true;
        } else {
            cdsAppDeploymentsList = cdsAppDeploymentSet.getApplicationDeploymentsList();
        }

        boolean doesExist;
        boolean success;

        for (ApplicationDeployment localAppDeployment: localAppDeploymentList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = cdsAppDeploymentsList.contains(localAppDeployment);
            }

            success = pushAppDeployment(localAppDeployment, doesExist);

            if (success){
                entityIds.add(localAppDeployment.getId().getValue());
            }
        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_APPLICATION_DEPLOYMENT, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_APPLICATION_DEPLOYMENT, DATA_UPDATE, DATA_SET_COMPLETE, entityIds );
        }


    }

    @Override
    public boolean pushAppDeployment(ApplicationDeployment appDeployment, boolean doesExist){
        RestResponse<ApplicationDeployment> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = applicationClient.createAppDeployment(appDeployment);
            if (restResponse.success){
                success = true;
            } else if(restResponse.responseCode == 409){
                pushAppDeployment(appDeployment, true);
            }
        } else {
            restResponse = applicationClient.updateAppDeployment(appDeployment.getId().getValue(), appDeployment);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushAppDeployment(appDeployment, false);
            }
        }

        return success;
    }

}
