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

import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.data.dao.*;
import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationDeployment;
import com.denimgroup.threadfix.data.entities.astam.AstamApplicationEnvironment;
import com.denimgroup.threadfix.data.entities.astam.AstamRawDiscoveredAttackSurface;
import com.denimgroup.threadfix.data.enums.AstamEntityType;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.denimgroup.threadfix.data.enums.AstamEntityType.FINDING;

/**
 * Created by amohammed on 7/10/2017.
 */

@Service
public class UuidUpdaterImpl implements UuidUpdater {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(UuidUpdaterImpl.class);

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private FindingDao findingDao;

    @Autowired
    private ChannelTypeDao channelTypeDao;

    @Autowired
    private WebAttackSurfaceDao attackSurfaceDao;

    @Autowired
    private ApplicationVersionDao applicationVersionDao;

    @Autowired
    private AstamApplicationEnvironmentDao applicationEnvironmentDao;

    @Autowired
    private AstamApplicationDeploymentDao applicationDeploymentDao;

    @Autowired
    private AstamRawDiscoveredAttackSurfaceDao rawDiscoveredAttackSurfaceDao;


    public UuidUpdaterImpl(){}

    /**
     * @param id This represents the object's original id in threadfix
     * @param newUuid represents the new random UUID assigned to the object, when first pushed to CDS
     * @param astamEntityType
     */

    @Override
    public void updateUUID(int id, String newUuid, AstamEntityType astamEntityType ){

        switch (astamEntityType){
            case APP_REGISTRATION:
                Application application = applicationDao.retrieveById(id);
                application.setUuid(newUuid);
                applicationDao.saveOrUpdate(application);
                break;
            case SAST_FINDING:
                updateUUID(id, newUuid, FINDING);
                break;
            case DAST_FINDING:
                updateUUID(id, newUuid, FINDING);
                break;
            case FINDING:
                Finding finding = findingDao.retrieveById(id);
                finding.setUuid(newUuid);
                findingDao.saveOrUpdate(finding);
                break;
            case EXTERNAL_TOOL:
                ChannelType channelType = channelTypeDao.retrieveById(id);
                channelType.setUuid(newUuid);
                channelTypeDao.saveOrUpdate(channelType);
                break;
            case RAW_FINDING:
                break;
            case CORRELATED_FINDING:
                //TODO:
                break;
            case ENTRY_POINT_WEB:
                WebAttackSurface attacksurface = attackSurfaceDao.retrieveById(id);
                attacksurface.setUuid(newUuid);
                attackSurfaceDao.saveOrUpdate(attacksurface);
                break;
            case APP_VERSION:
                ApplicationVersion appVersion = applicationVersionDao.retrieveById(id);
                appVersion.setUuid(newUuid);
                applicationVersionDao.saveOrUpdate(appVersion);
                break;
            case APP_DEPLOYMENT:
                AstamApplicationDeployment appDeployment = applicationDeploymentDao.retrieveById(id);
                appDeployment.setUuid(newUuid);
                applicationDeploymentDao.saveOrUpdate(appDeployment);
                break;
            case APP_ENVIRONMENT:
                AstamApplicationEnvironment appEnvironment = applicationEnvironmentDao.retrieveById(id);
                appEnvironment.setUuid(newUuid);
                applicationEnvironmentDao.saveOrUpdate(appEnvironment);
                break;
            case RAW_DISCOVERED_ATTACK_SURFACE:
                AstamRawDiscoveredAttackSurface rawDiscoveredAttackSurface = rawDiscoveredAttackSurfaceDao.retrieveById(id);
                rawDiscoveredAttackSurface.setUuid(newUuid);
                rawDiscoveredAttackSurfaceDao.saveOrUpdate(rawDiscoveredAttackSurface);
                break;
        }

        LOGGER.info("Updated local " + astamEntityType + " Id: " + id + " with UUID: " + newUuid);


    }
}
