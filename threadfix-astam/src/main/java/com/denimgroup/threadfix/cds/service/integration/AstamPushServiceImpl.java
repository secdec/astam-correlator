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
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.denimgroup.threadfix.mapper.AstamEntitiesMapper;
import com.denimgroup.threadfix.mapper.AstamFindingsMapper;
import com.secdec.astam.common.data.models.Appmgmt.ApplicationRegistration;
import com.secdec.astam.common.data.models.Attacksurface;
import com.secdec.astam.common.data.models.Entities;
import com.secdec.astam.common.data.models.Findings;
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
        List<Application> applicationList = applicationDao.retrieveAllActive();
        for (int i=0; i<applicationList.size(); i++) {
            Application app = applicationList.get(i);
            pushSingleAppToAstam(app);
        }
    }

    @Override
    public void pushSingleAppToAstam(Application app){
        int appId = app.getId();
        pushAppMngmtToAstam(appId);
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
        ApplicationRegistration appRegistration = astamApplicationService.getAppRegistration(applicationId);
        applicationPushService.pushApplicationToAstam(applicationId, appRegistration);
    }

    @Override
    public void pushFindingsToAstam(int applicationId){
        AstamFindingsMapper astamMapper = new AstamFindingsMapper(applicationId);
        Findings.SastFindingSet sastFindingSet = astamFindingsService.getSastFindings(astamMapper);
        Findings.DastFindingSet dastFindingSet = astamFindingsService.getDastFindings(astamMapper);
        //TODO: Findings.RawFindingsSet rawFindingsSet =
        Findings.CorrelatedFindingSet correlatedFindingSet = astamFindingsService.getCorrelatedFindings(astamMapper);
        findingsPushService.pushFindingsToAstam(sastFindingSet, dastFindingSet, correlatedFindingSet);
    }

    @Override
    public void pushAttackSurfaceToAstam(int applicationId){
        AstamAttackSurfaceMapper astamMapper = new AstamAttackSurfaceMapper(applicationId );
        Attacksurface.EntryPointWebSet entryPointWebSet = astamAttackSurfaceService.getEntryPointWebSet(astamMapper, applicationId);

        //Attacksurface.EntryPointMobileSet entryPointMobileSet =
        //Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface = astamAttackSurfaceService.getRawDiscoveredAttackSurface(astamMapper);
        //entryPointWebSet
        attackSurfacePushService.pushAttackSurfaceToAstam(entryPointWebSet);
    }
}

