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

package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.cds.messaging.AstamMessageManager;
import com.denimgroup.threadfix.cds.service.AstamApplicationImporter;
import com.denimgroup.threadfix.cds.service.AstamPushService;
import com.denimgroup.threadfix.data.dao.AstamConfigurationDao;
import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataAction.*;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataEntity.DATA_APPLICATION_REGISTRATION;
import static com.secdec.astam.common.messaging.Messaging.AstamMessage.DataMessage.DataSetType.DATA_SET_SINGLE;

/**
 * Created by amohammed on 7/23/2017.
 */
@Service
public class AstamConfigurationServiceImpl implements AstamConfigurationService {

    protected final SanitizedLogger log = new SanitizedLogger(AstamConfigurationServiceImpl.class);

    @Autowired
    private AstamConfigurationDao astamConfigurationDao;

    @Autowired
    private AstamPushService astamPushService;

    @Autowired
    private AstamApplicationImporter astamApplicationImporter;

    @Transactional(readOnly = false)
    @Override
    public AstamConfiguration loadCurrentConfiguration() {
        AstamConfiguration configuration = astamConfigurationDao.loadCurrentConfiguration();

        assert configuration != null;

        return configuration;
    }

    @Override
    public void saveConfiguration(AstamConfiguration config) {
        astamConfigurationDao.saveOrUpdate(config);
        //TODO: (edge case) figure out pushing existing data prior to configuration,
        // only if applications will also be created in the ASTAM correlator
        //astamPushService.pushAllToAstam();
        astamApplicationImporter.importAllApplications();
        AstamMessageManager messageManager = new AstamMessageManager(config);
        messageManager.subscribe(DATA_APPLICATION_REGISTRATION, DATA_CREATE, DATA_SET_SINGLE);
        messageManager.subscribe(DATA_APPLICATION_REGISTRATION, DATA_UPDATE, DATA_SET_SINGLE);
        messageManager.subscribe(DATA_APPLICATION_REGISTRATION, DATA_DELETE, DATA_SET_SINGLE);
    }
}
