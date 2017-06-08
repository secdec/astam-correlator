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
