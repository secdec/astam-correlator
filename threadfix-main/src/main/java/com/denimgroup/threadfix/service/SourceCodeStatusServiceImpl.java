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

import com.denimgroup.threadfix.data.dao.GenericObjectDao;
import com.denimgroup.threadfix.data.dao.SourceCodeStatusDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeRepoType;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/** @author jrios */
@Service
@Transactional(readOnly = false)
public class SourceCodeStatusServiceImpl extends AbstractGenericObjectService<SourceCodeStatus> implements SourceCodeStatusService {

    protected final SanitizedLogger log = new SanitizedLogger(SourceCodeStatusServiceImpl.class);

    @Autowired
    SourceCodeStatusDao dao;

    @Override
    public SourceCodeStatus getLatest(Application application) {
        return dao.getLatest(application);
    }

    @Override
    public void saveNewStatus(Application application,String commitId) {
        SourceCodeStatus newSourceCodeStatus = new SourceCodeStatus();
        newSourceCodeStatus.setApplication(application);
        newSourceCodeStatus.setRepoType(SourceCodeRepoType.getType(application.getRepositoryType()));
        newSourceCodeStatus.setDateTimeChecked(new Date());
        newSourceCodeStatus.setCommitId(commitId);
        newSourceCodeStatus.setBranch(application.getResolvedRepositoryBranch());
        saveOrUpdate(newSourceCodeStatus);
        log.debug("Source Code status updated:" + newSourceCodeStatus.toString());
    }

    @Override
    GenericObjectDao<SourceCodeStatus> getDao() {
        return (GenericObjectDao<SourceCodeStatus>)dao;
    }
}
