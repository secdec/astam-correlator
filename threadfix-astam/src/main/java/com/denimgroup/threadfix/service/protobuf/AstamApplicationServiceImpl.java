package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.mapper.AstamApplicationMapper;
import com.denimgroup.threadfix.service.AstamApplicationService;
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
}
