package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.denimgroup.threadfix.service.AstamAttackSurfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class AstamAttackSurfaceServiceImpl implements AstamAttackSurfaceService {
    private final WebAttackSurfaceDao attackSurfaceDao;

    @Autowired
    public AstamAttackSurfaceServiceImpl(WebAttackSurfaceDao attackSurfaceDao, ScanDao scanDao) {
        this.attackSurfaceDao = attackSurfaceDao;
    }

    @Override
    public void writeAttackSurfaceToOutput(AstamAttackSurfaceMapper mapper, OutputStream outputStream)
            throws IOException {
        List<WebAttackSurface> attackSurfaces = attackSurfaceDao.retrieveWebAttackSurfaceByAppId(mapper.getApplicationId());

        mapper.addWebEntryPoints(attackSurfaces);

        mapper.writeAttackSurfaceToOutput(outputStream);
    }
}
