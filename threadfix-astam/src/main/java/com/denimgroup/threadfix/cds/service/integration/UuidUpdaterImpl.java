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

import com.denimgroup.threadfix.cds.service.UuidUpdater;
import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.dao.ChannelTypeDao;
import com.denimgroup.threadfix.data.dao.FindingDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ChannelType;
import com.denimgroup.threadfix.data.entities.Finding;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
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


    public UuidUpdaterImpl(/*ApplicationDao applicationDao,
                           FindingDao findingDao,
                           ChannelTypeDao channelTypeDao,
                           WebAttackSurfaceDao attackSurfaceDao*/){
      /*  this.applicationDao = applicationDao;
        this.findingDao = findingDao;
        this.channelTypeDao = channelTypeDao;
        this.attackSurfaceDao = attackSurfaceDao;*/
    }

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
                LOGGER.info("Updating local application id: " + id + ", adding uuid from CDS. UUID:" + newUuid);
                applicationDao.saveOrUpdate(application);
                break;
            case SAST_FINDING:
                LOGGER.info("Updating local SAST finding id: " + id + ", adding uuid from CDS. UUID:" + newUuid);
                updateUUID(id, newUuid, FINDING);
                break;
            case DAST_FINDING:
                LOGGER.info("Updating local DAST finding id: " + id + ", adding uuid from CDS. UUID:" + newUuid);
                updateUUID(id, newUuid, FINDING);
                break;
            case FINDING:
                Finding finding = findingDao.retrieveById(id);
                finding.setUuid(newUuid);
                findingDao.saveOrUpdate(finding);
                break;
            case EXTERNAL_TOOL:
                LOGGER.info("Updating local ExternalTool/ChannelType id: " + id + ", adding uuid from CDS. UUID:" + newUuid);
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
                LOGGER.info("Updating local WebAttackSurface id: " + id + ", adding uuid from CDS. UUID:" + newUuid);
                WebAttackSurface attacksurface = attackSurfaceDao.retrieveById(id);
                attacksurface.setUuid(newUuid);
                attackSurfaceDao.saveOrUpdate(attacksurface);
                break;

        }
    }
}
