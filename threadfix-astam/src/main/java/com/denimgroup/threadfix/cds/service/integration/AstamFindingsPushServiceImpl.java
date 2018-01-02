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

package com.denimgroup.threadfix.cds.service.integration;

import com.denimgroup.threadfix.cds.messaging.AstamMessageManager;
import com.denimgroup.threadfix.cds.rest.AstamFindingsClient;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.cds.service.AstamFindingsPushService;
import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Findings.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.data.enums.AstamEntityType.*;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_CREATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.DATA_UPDATE;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.*;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType.DATA_SET_COMPLETE;

/**
 * Created by amohammed on 6/29/2017.
 */
@Service
public class AstamFindingsPushServiceImpl implements AstamFindingsPushService {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamFindingsPushServiceImpl.class);

    private AstamFindingsClient findingsClient;
    private AstamMessageManager messageNotifier;
    private UuidUpdater uuidUpdater;

    @Autowired
    public AstamFindingsPushServiceImpl(AstamFindingsClient findingsClient,
                                        AstamMessageManager messageManager,
                                        UuidUpdater uuidUpdater){

        this.findingsClient = findingsClient;
        this.messageNotifier = messageManager;
        this.uuidUpdater = uuidUpdater;
    }

    @Override
    public void pushSastFindingSet(SastFindingSet localSastFindingSet) {

        List<SastFinding> localSastFindingList = localSastFindingSet.getSastFindingsList();
        SastFindingSet CDSSastFindingSet = null;
        List<SastFinding> CDSSastFindingList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            CDSSastFindingSet = findingsClient.getAllSastFindings().object;
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(CDSSastFindingSet == null || CDSSastFindingSet.getSastFindingsList().isEmpty() ){
            isCreateOperation = true;
        } else {
            CDSSastFindingList = CDSSastFindingSet.getSastFindingsList();
        }

        boolean doesExist;
        boolean success;

        for (SastFinding localSastFinding: localSastFindingList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist = CDSSastFindingList.contains(localSastFinding);
            }

            success = pushSastFinding(localSastFinding, doesExist);

            if(success){
                entityIds.add(localSastFinding.getId().getValue());
            }
        }

        if (isCreateOperation) {
            messageNotifier.notify(DATA_SAST_FINDING, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        } else {
            messageNotifier.notify(DATA_SAST_FINDING, DATA_CREATE, DATA_SET_COMPLETE, entityIds);
        }

    }

    @Override
    public boolean pushSastFinding(SastFinding sastFinding, boolean doesExist) {
        RestResponse<SastFinding> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = findingsClient.createSastFinding(sastFinding);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(sastFinding.getId().getValue());
                uuidUpdater.updateUUID(id, restResponse.uuid, SAST_FINDING);
            } else if(restResponse.responseCode == 409){
                success = pushSastFinding(sastFinding, true);
            }
        } else {
            restResponse = findingsClient.updateSastFinding(sastFinding.getId().getValue(), sastFinding);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushSastFinding(sastFinding, false);
            }
        }
        return success;
    }

    @Override
    public void pushDastFindingSet(DastFindingSet localDastFindingSet) {
        List<DastFinding> localDastFindingList = localDastFindingSet.getDastFindingsList();
        DastFindingSet CDSDastFindingSet = null;
        List<DastFinding> CDSDastFindingList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            CDSDastFindingSet = findingsClient.getAllDastFindings().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(CDSDastFindingSet == null || CDSDastFindingSet.getDastFindingsList().isEmpty() ){
            isCreateOperation = true;
        } else {
            CDSDastFindingList = CDSDastFindingSet.getDastFindingsList();
        }

        boolean doesExist;
        boolean success;

        for (DastFinding localDastFinding: localDastFindingList){
            doesExist = false;
            success = false;

            if(!isCreateOperation) {
                doesExist = CDSDastFindingList.contains(localDastFinding);
            }

            success = pushDastFinding(localDastFinding, doesExist);

            if (success){
                entityIds.add(localDastFinding.getId().getValue());
            }
        }
        if (isCreateOperation){
            messageNotifier.notify(DATA_DAST_FINDING, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_DAST_FINDING, DATA_UPDATE, DATA_SET_COMPLETE, entityIds );
        }
    }

    @Override
    public boolean pushDastFinding(DastFinding dastFinding, boolean doesExist) {
        RestResponse<DastFinding> restResponse;
        boolean success = false;

        if (!doesExist){

            restResponse = findingsClient.createDastFinding(dastFinding);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(dastFinding.getId().getValue());
                uuidUpdater.updateUUID(id, restResponse.uuid, DAST_FINDING);
            } else if(restResponse.responseCode == 409){
                success = pushDastFinding(dastFinding, true);
            }

        } else {
            restResponse = findingsClient.updateDastFinding(dastFinding.getId().getValue(), dastFinding);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushDastFinding(dastFinding, false);
            }

        }
        return success;
    }

    @Override
    public void pushRawFindingsSet(RawFindingsSet localRawFindingsSet) {
        List<RawFindings> localRawFindingsList = localRawFindingsSet.getRawFindingsList();
        RawFindingsSet CDSRawFindingsSet = null;
        List<RawFindings> CDSRawFindingsList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            CDSRawFindingsSet = findingsClient.getAllRawFindings().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(CDSRawFindingsSet == null || CDSRawFindingsSet.getRawFindingsList().isEmpty() ){
            isCreateOperation = true;
        } else {
            CDSRawFindingsList = CDSRawFindingsSet.getRawFindingsList();
        }


        boolean doesExist;
        boolean success;

        for (RawFindings localRawFindings: localRawFindingsList){
            success = false;
            doesExist = false;

            if(!isCreateOperation){
                doesExist = CDSRawFindingsList.contains(localRawFindings);
            }

            success = pushRawFindings(localRawFindings, doesExist);

            if(success) {
                entityIds.add(localRawFindings.getId().getValue());
            }

        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_RAW_FINDINGS, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_RAW_FINDINGS, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        }
    }

    @Override
    public boolean pushRawFindings(RawFindings rawFindings, boolean doesExist) {
        RestResponse<RawFindings> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = findingsClient.createRawFindings(rawFindings);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(rawFindings.getId().getValue());
                uuidUpdater.updateUUID(id, restResponse.uuid, RAW_FINDING);
            } else if(restResponse.responseCode == 409){
                success = pushRawFindings(rawFindings, true);
            }
        } else {
            restResponse = findingsClient.updateRawFindings(rawFindings.getId().getValue(), rawFindings);
            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushRawFindings(rawFindings, false);
            }

        }
        return success;
    }

    @Override
    public void pushCorrelatedFindingSet(CorrelatedFindingSet localCorrelatedFindingSet) {
        List<CorrelatedFinding> localCorrelatedFindingList = localCorrelatedFindingSet.getCorrelatedFindingsList();
        CorrelatedFindingSet CDSCorrelatedFindingSet = null;
        List<CorrelatedFinding> CDSCorrelatedFindingList = null;
        List<String> entityIds = new ArrayList<>();
        boolean isCreateOperation = false;

        try {
            CDSCorrelatedFindingSet = findingsClient.getAllCorrelatedFindings().getObject();
        } catch (NullPointerException npe){
            LOGGER.debug("Data set does not exist in CDS. Attempting to create ...");
        }

        if(CDSCorrelatedFindingSet == null || CDSCorrelatedFindingSet.getCorrelatedFindingsList().isEmpty() ) {
            isCreateOperation = true;
        } else {
            CDSCorrelatedFindingList = CDSCorrelatedFindingSet.getCorrelatedFindingsList();
        }

        boolean success;
        boolean doesExist;

        for (CorrelatedFinding localCorrelatedFinding: localCorrelatedFindingList){
            doesExist = false;
            success = false;

            if(!isCreateOperation){
                doesExist =  CDSCorrelatedFindingList.contains(localCorrelatedFinding);
            }

            success = pushCorrelatedFinding(localCorrelatedFinding, doesExist);
            if(success){
                entityIds.add(localCorrelatedFinding.getId().getValue());
            }
        }

        if(isCreateOperation){
            messageNotifier.notify(DATA_CORRELATED_FINDING, DATA_CREATE, DATA_SET_COMPLETE, entityIds );
        } else {
            messageNotifier.notify(DATA_CORRELATED_FINDING, DATA_UPDATE, DATA_SET_COMPLETE, entityIds);
        }
    }

    @Override
    public boolean pushCorrelatedFinding(CorrelatedFinding correlatedFinding, boolean doesExist) {
        RestResponse<RawFindings> restResponse;
        boolean success = false;

        if (!doesExist){
            restResponse = findingsClient.createCorrelatedFinding(correlatedFinding);
            if (restResponse.success){
                success = true;
                int id = ProtobufMessageUtils.createIdFromUUID(correlatedFinding.getId().getValue());
                uuidUpdater.updateUUID(id, restResponse.uuid, CORRELATED_FINDING);
            } else if(restResponse.responseCode == 409){
                success = pushCorrelatedFinding(correlatedFinding, true);
            }
        } else {
            restResponse = findingsClient.updateCorrelatedFinding(
                    correlatedFinding.getId().getValue(),
                    correlatedFinding);

            if (restResponse.success) {
                success = true;
            } else if(restResponse.responseCode == 422){
                success = pushCorrelatedFinding(correlatedFinding, false);
            }
        }

        return success;
    }

}
