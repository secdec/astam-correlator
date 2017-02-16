package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.FindingDao;
import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.entities.Finding;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.denimgroup.threadfix.service.AstamAttackSurfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class AstamAttackSurfaceServiceImpl implements AstamAttackSurfaceService {
    private final FindingDao findingDao;
    private final ScanDao scanDao;

    @Autowired
    public AstamAttackSurfaceServiceImpl(FindingDao findingDao, ScanDao scanDao) {
        this.findingDao = findingDao;
        this.scanDao = scanDao;
    }

    @Override
    public void writeAttackSurfaceToOutput(AstamAttackSurfaceMapper mapper, OutputStream outputStream)
            throws IOException {
        List<Finding> findings = findingDao.retrieveFindingsWithHAMEndpointByAppId(mapper.getApplicationId());

        mapper.addWebEntryPoints(findings);

        mapper.writeAttackSurfaceToOutput(outputStream);
    }
}
