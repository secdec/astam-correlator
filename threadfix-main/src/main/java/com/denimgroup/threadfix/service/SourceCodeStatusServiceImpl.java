package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.dao.GenericObjectDao;
import com.denimgroup.threadfix.data.dao.SourceCodeStatusDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/** @author jrios */
@Service
@Transactional(readOnly = false)
public class SourceCodeStatusServiceImpl extends AbstractGenericObjectService<SourceCodeStatus> implements SourceCodeStatusService {

    @Autowired
    SourceCodeStatusDao dao;

    @Override
    public SourceCodeStatus getLatest(Application application) {
        return dao.getLatest(application);
    }

    @Override
    GenericObjectDao<SourceCodeStatus> getDao() {
        return (GenericObjectDao<SourceCodeStatus>)dao;
    }
}
