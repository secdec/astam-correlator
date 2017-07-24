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
import com.denimgroup.threadfix.cds.rest.AstamEntitiesClient;
import com.denimgroup.threadfix.cds.rest.Impl.AstamEntitiesClientImpl;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.cds.service.AstamEntitiesPushService;
import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.data.dao.AstamConfigurationDao;
import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Entities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.data.enums.AstamEntityType.EXTERNAL_TOOL;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_CREATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_UPDATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.DATA_CWE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.DATA_EXTERNAL_TOOL;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType.DATA_SET_COMPLETE;

/**
 * Created by amohammed on 7/14/2017.
 */
@Service
public class AstamEntitiesPushServiceImpl implements AstamEntitiesPushService {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamEntitiesPushServiceImpl.class);

    private AstamEntitiesClient astamEntitiesClient;
    private AstamMessageManager messageNotifier;
    private UuidUpdater uuidUpdater;

    @Autowired
    public AstamEntitiesPushServiceImpl(AstamConfigurationDao astamConfigurationDao, UuidUpdater uuidUpdater){
        this.uuidUpdater = uuidUpdater;
        AstamConfiguration astamConfig = astamConfigurationDao.loadCurrentConfiguration();
        this.astamEntitiesClient = new AstamEntitiesClientImpl(astamConfig);
        this.messageNotifier = new AstamMessageManager(astamConfig);
    }

    @Override
    public void pushEntitiesToAstam(Entities.ExternalToolSet externalToolSet){
        pushExternalToolsSet(externalToolSet);
        //pushCweSet(cweSet);
    }

    @Override
    public void pushExternalToolsSet(Entities.ExternalToolSet externalToolSet) {

        List<Entities.ExternalTool> externalToolList = externalToolSet.getExternalToolsList();
        Entities.ExternalToolSet CDSExternalToolsSet = null;
        List<Entities.ExternalTool> CDSExternalToolsList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            CDSExternalToolsSet = astamEntitiesClient.getAllExternalTools().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(CDSExternalToolsSet == null || CDSExternalToolsSet.getExternalToolsList().isEmpty() ){
            isCreateOperation = true;
        } else {
            CDSExternalToolsList = CDSExternalToolsSet.getExternalToolsList();
        }

        boolean doesExist;
        boolean success;

        for (Entities.ExternalTool externalTool : externalToolList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = CDSExternalToolsList.contains(externalTool);
            }

            success = pushExternalTool(externalTool, doesExist);

            if(success){
                entityIds.add(externalTool.getId().getValue());
            }
        }

        if (isCreateOperation) {
            messageNotifier.notify(DATA_EXTERNAL_TOOL, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        } else {
            messageNotifier.notify(DATA_EXTERNAL_TOOL, DATA_CREATE, DATA_SET_COMPLETE, entityIds);
        }

    }

    @Override
    public boolean pushExternalTool(Entities.ExternalTool externalTool, boolean doesExist) {
        RestResponse<Entities.ExternalTool> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = astamEntitiesClient.createExternalTool(externalTool);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(externalTool.getId().getValue());
                uuidUpdater.updateUUID(id, externalTool.getId().getValue(), EXTERNAL_TOOL);
            } else if(restResponse.responseCode == 409){
                pushExternalTool(externalTool, true);
            }
        } else {
            restResponse = astamEntitiesClient.updateExternalTool(externalTool.getId().getValue(), externalTool);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushExternalTool(externalTool, false);
            }
        }
        return success;
    }

    @Override
    public void pushCweSet(Entities.CWESet cweSet) {

        List<Entities.CWE> localCwesList = cweSet.getCwesList();
        Entities.CWESet cdsCweSet = null;
        List<Entities.CWE> cdsCweList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            cdsCweSet = astamEntitiesClient.getAllCWEs().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(cdsCweSet == null || cdsCweSet.getCwesList().isEmpty() ){
            isCreateOperation = true;
        } else {
            cdsCweList = cdsCweSet.getCwesList();
        }

        boolean doesExist;
        boolean success;

        for (Entities.CWE localCwe: localCwesList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = cdsCweList.contains(localCwe);
            }

            success = pushCwe(localCwe, doesExist);

            if(success){
                entityIds.add(String.valueOf(localCwe.getCweId()));
            }
        }

        if (isCreateOperation) {
            messageNotifier.notify(DATA_CWE, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        } else {
            messageNotifier.notify(DATA_CWE, DATA_CREATE, DATA_SET_COMPLETE, entityIds);
        }

    }

    @Override
    public boolean pushCwe(Entities.CWE cwe, boolean doesExist) {
        RestResponse<Entities.CWE> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = astamEntitiesClient.createCWE(cwe);
            if (restResponse.success){
                success = true;
            } else if(restResponse.responseCode == 409){
                pushCwe(cwe, true);
            }
        } else {
            restResponse = astamEntitiesClient.updateCWE(String.valueOf(cwe.getCweId()), cwe);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                pushCwe(cwe, false);
            }
        }
        return success;
    }



}


