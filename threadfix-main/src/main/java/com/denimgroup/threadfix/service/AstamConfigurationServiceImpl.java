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

package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.dao.AstamConfigurationDao;
import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by amohammed on 7/23/2017.
 */
@Service
@Transactional(readOnly = true)
public class AstamConfigurationServiceImpl implements AstamConfigurationService {

    protected final SanitizedLogger log = new SanitizedLogger(AstamConfigurationServiceImpl.class);

    @Autowired
    private AstamConfigurationDao astamConfigurationDao;


    @Override
    public AstamConfiguration loadCurrentConfiguration() {
        AstamConfiguration configuration = astamConfigurationDao.loadCurrentConfiguration();
        assert configuration != null;
        return configuration;
    }

    @Override
    @Transactional(readOnly = false)
    public void saveConfiguration(AstamConfiguration config) {
        astamConfigurationDao.saveOrUpdate(config);
    }
}
