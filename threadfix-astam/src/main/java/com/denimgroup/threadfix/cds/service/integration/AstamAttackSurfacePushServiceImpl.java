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
import com.denimgroup.threadfix.cds.rest.AstamAttackSurfaceClient;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.cds.service.AstamAttackSurfacePushService;
import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Attacksurface;
import com.secdec.astam.common.data.models.Attacksurface.EntryPointWeb;
import com.secdec.astam.common.data.models.Attacksurface.EntryPointWebSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.data.enums.AstamEntityType.ENTRY_POINT_WEB;
import static com.denimgroup.threadfix.data.enums.AstamEntityType.RAW_DISCOVERED_ATTACK_SURFACE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_CREATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_UPDATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.DATA_ENTRY_POINT_WEB;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType.DATA_SET_COMPLETE;


/**
 * Created by amohammed on 6/29/2017.
 */

@Service
public class AstamAttackSurfacePushServiceImpl implements AstamAttackSurfacePushService {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamAttackSurfacePushServiceImpl.class);

    private AstamAttackSurfaceClient attackSurfaceClient;
    private AstamMessageManager messageNotifier;
    private UuidUpdater uuidUpdater;

    @Autowired
    public AstamAttackSurfacePushServiceImpl(AstamAttackSurfaceClient attackSurfaceClient,
                                             AstamMessageManager messageManager,
                                             UuidUpdater uuidUpdater){

        this.attackSurfaceClient = attackSurfaceClient;
        this.messageNotifier = messageManager;
        this.uuidUpdater = uuidUpdater;
    }

    @Override
    public void pushAttackSurfaceToAstam(EntryPointWebSet entryPointWebSet){
        pushEntryPointWebSet(entryPointWebSet);
        //TODO:
        //pushEntryPointMobileSet();
        //pushRawDiscoveredAttackSurfaceSet();
    }

    @Override
    public void pushEntryPointWebSet(EntryPointWebSet localEntryPointWebSet) {
        List<EntryPointWeb> localEntryPointWebList = localEntryPointWebSet.getWebEntryPointsList();
        EntryPointWebSet CDSEntryPointWebSet = null;
        List<EntryPointWeb> CDSEntryPointWebList = null;
        List<String> entityIds = new ArrayList();
        boolean isCreateOperation = false;

        try {
            CDSEntryPointWebSet = attackSurfaceClient.getAllEntryPointsWeb().getObject();
            CDSEntryPointWebList = CDSEntryPointWebSet.getWebEntryPointsList();
        }catch (NullPointerException e){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }


        if(CDSEntryPointWebList == null || CDSEntryPointWebList.isEmpty()) {
            isCreateOperation = true;
        }

        boolean doesExist;
        boolean success;
        for (EntryPointWeb localEntryPointWeb: localEntryPointWebList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = CDSEntryPointWebList.contains(localEntryPointWeb);
            }

            success = pushEntryPointWeb(localEntryPointWeb, doesExist);
            if(success) {
                entityIds.add(localEntryPointWeb.getId().getValue());
            }
        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_ENTRY_POINT_WEB, DATA_CREATE, DATA_SET_COMPLETE, entityIds);
        } else {
            messageNotifier.notify(DATA_ENTRY_POINT_WEB, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        }

    }

    @Override
    public boolean pushEntryPointWeb(EntryPointWeb entryPointWeb, boolean doesExist){
        RestResponse<EntryPointWeb> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = attackSurfaceClient.createEntryPointWeb(entryPointWeb);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(entryPointWeb.getId().getValue());
                uuidUpdater.updateUUID(id, restResponse.uuid, ENTRY_POINT_WEB);
            } else if(restResponse.responseCode == 409){
                success = pushEntryPointWeb(entryPointWeb, true);
            }
        } else {
            restResponse = attackSurfaceClient.updateEntryPointWeb(entryPointWeb.getId().getValue(), entryPointWeb);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushEntryPointWeb(entryPointWeb, false);
            }

        }
        return success;
    }

    @Override
    public boolean pushRawDiscoveredAttackSurface(Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface, boolean doesExist){
        RestResponse<EntryPointWeb> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = attackSurfaceClient.createRawDiscoveredAttackSurface(rawDiscoveredAttackSurface);
            if (restResponse.success){
                success = true;
                  int id = ProtobufMessageUtils.createIdFromUUID(rawDiscoveredAttackSurface.getId().getValue());
                   uuidUpdater.updateUUID(id, restResponse.uuid, RAW_DISCOVERED_ATTACK_SURFACE);
            } else if(restResponse.responseCode == 409){
                success = pushRawDiscoveredAttackSurface(rawDiscoveredAttackSurface, true);
            }
        } else {
            restResponse = attackSurfaceClient.updateRawDiscoveredAttackSurface(rawDiscoveredAttackSurface.getId().getValue(), rawDiscoveredAttackSurface);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushRawDiscoveredAttackSurface(rawDiscoveredAttackSurface, false);
            }

        }
        return success;
    }
}
