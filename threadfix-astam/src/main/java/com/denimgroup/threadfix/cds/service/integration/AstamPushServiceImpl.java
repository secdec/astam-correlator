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

import com.denimgroup.threadfix.cds.service.AstamPushService;
import com.denimgroup.threadfix.cds.service.protobuf.AstamRemoteApplicationServiceImpl;
import com.denimgroup.threadfix.cds.service.protobuf.AstamRemoteAttackSurfaceServiceImpl;
import com.denimgroup.threadfix.cds.service.protobuf.AstamRemoteFindingsServiceImpl;
import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.mapper.AstamEntitiesMapper;
import com.secdec.astam.common.data.models.Appmgmt.ApplicationRegistration;
import com.secdec.astam.common.data.models.Entities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by amohammed on 6/29/2017.
 */
@Service
public class AstamPushServiceImpl implements AstamPushService {

    private final ApplicationDao applicationDao;
    private final AstamRemoteApplicationServiceImpl astamApplicationService;
    private final AstamRemoteFindingsServiceImpl astamFindingsService;
    private final AstamRemoteAttackSurfaceServiceImpl astamAttackSurfaceService;

    private final AstamApplicationPushServiceImpl applicationPushService;
    private final AstamAttackSurfacePushServiceImpl attackSurfacePushService;
    private final AstamFindingsPushServiceImpl findingsPushService;
    private final AstamEntitiesPushServiceImpl astamEntitiesPushService;

    @Autowired
    public AstamPushServiceImpl(ApplicationDao applicationDao,
                                AstamRemoteApplicationServiceImpl astamApplicationService,
                                AstamRemoteFindingsServiceImpl astamFindingsService,
                                AstamRemoteAttackSurfaceServiceImpl astamAttackSurfaceService,
                                AstamEntitiesPushServiceImpl astamEntitiesPushService,
                                AstamApplicationPushServiceImpl astamApplicationPushService,
                                AstamAttackSurfacePushServiceImpl astamAttackSurfacePushService,
                                AstamFindingsPushServiceImpl astamFindingsPushService) {

        this.applicationDao = applicationDao;
        this.astamApplicationService = astamApplicationService;
        this.astamFindingsService = astamFindingsService;
        this.astamAttackSurfaceService = astamAttackSurfaceService;

        this.astamEntitiesPushService = astamEntitiesPushService;
        this.applicationPushService = astamApplicationPushService;
        this.attackSurfacePushService = astamAttackSurfacePushService;
        this.findingsPushService = astamFindingsPushService;
    }

    @Override
    public void pushAllToAstam(){
        List<Application> applicationList = applicationDao.retrieveAll();
        for (int i=0; i<applicationList.size(); i++) {
            Application app = applicationList.get(i);
            pushSingleAppToAstam(app);
        }
    }

    @Override
    public void pushSingleAppToAstam(Application app){
        int appId = app.getId();
        pushAppMngmtToAstam(appId);

        //TODO:
        //pushEntitiesToAstam(app);
        //pushAttackSurfaceToAstam(appId);
        //pushFindingsToAstam(appId);
    }

    @Override
    public void pushEntitiesToAstam(Application app){
        AstamEntitiesMapper astamMapper = new AstamEntitiesMapper();
        Entities.ExternalToolSet externalToolSet = astamMapper.getExternalToolSet(app);
        astamEntitiesPushService.pushEntitiesToAstam(externalToolSet);
    }

    @Override
    public void pushAppMngmtToAstam(int applicationId) {

        astamApplicationService.setup(applicationId);

        boolean success = false;

        ApplicationRegistration appRegistration = astamApplicationService.getAppRegistration();
        applicationPushService.pushAppRegistration(applicationId, appRegistration);
       /*if(!success){
            return;
        }*/
        /*
        success = false;
        Appmgmt.ApplicationEnvironment appEnvironment = astamApplicationService.getAppEnvironment();
        success = applicationPushService.pushAppEnvironment(appEnvironment, false);
        if(!success){
            return;
        }

        success = false;
        Appmgmt.ApplicationVersion appVersion = astamApplicationService.getAppVersion();
        success = applicationPushService.pushAppVersion(appVersion, false);
        if(!success){
            return;
        }

        success = false;
        Appmgmt.ApplicationDeployment appDeployment = astamApplicationService.getAppDeployment();
        success = applicationPushService.pushAppDeployment(appDeployment, false);*/
    }

    @Override
    public void pushFindingsToAstam(int applicationId){
        //TODO:
      /*  astamFindingsService.setup(applicationId);
        boolean success = false;


        Findings.RawFindingsSet rawFindingsSet = astamFindingsService.getRawFindingsSet();
        findingsPushService.pushRawFindingsSet(rawFindingsSet);

        //Don't push Sast or Dast findings until RawFindings are pushed
        Findings.SastFindingSet sastFindingSet = astamFindingsService.getSastFindings();
        findingsPushService.pushSastFindingSet(sastFindingSet);

        Findings.DastFindingSet dastFindingSet = astamFindingsService.getDastFindings();
        findingsPushService.pushDastFindingSet(dastFindingSet);

        Findings.CorrelationResultSet correlationResultSet = astamFindingsService.getCorrelatedResultSet();
        findingsPushService.p

        Findings.CorrelatedFindingSet correlatedFindingSet = astamFindingsService.getCorrelatedFindings();
        findingsPushService.pushCorrelatedFindingSet(correlatedFindingSet);*/

    }

    @Override
    public void pushAttackSurfaceToAstam(int applicationId){
        //TODO:
     /*   astamApplicationService.setup(applicationId);

        boolean success = false;
        while(!success){
            Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface = astamAttackSurfaceService.getRawDiscoveredAttackSurface();
            success = attackSurfacePushService.pushRawDiscoveredAttackSurface(rawDiscoveredAttackSurface, false);
        }

        Attacksurface.EntryPointWebSet entryPointWebSet = astamAttackSurfaceService.getEntryPointWebSet();
        attackSurfacePushService.pushEntryPointWebSet(entryPointWebSet);*/
    }
}

