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
package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.mapper.AstamApplicationMapper;
import com.denimgroup.threadfix.service.AstamApplicationService;
import com.secdec.astam.common.data.models.Appmgmt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/12/2017.
 */
@Service
public class AstamApplicationServiceImpl implements AstamApplicationService {
    private final ApplicationDao applicationDao;

    @Autowired
    public AstamApplicationServiceImpl(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public void writeApplicationToOutput(int applicationId, OutputStream outputStream) throws IOException {
        AstamApplicationMapper appMapper = new AstamApplicationMapper();
        Application app = applicationDao.retrieveById(applicationId);

        appMapper.setApplication(app);
        appMapper.writeApplicationToOutput(outputStream);
    }

    public Appmgmt.ApplicationRegistration getAppRegistration(int applicationId){
        AstamApplicationMapper appMapper = new AstamApplicationMapper();
        Application app = applicationDao.retrieveById(applicationId);

        appMapper.setApplication(app);
        return appMapper.getAppRegistration();
    }

}
