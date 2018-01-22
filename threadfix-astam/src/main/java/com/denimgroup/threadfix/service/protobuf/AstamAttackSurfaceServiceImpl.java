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

import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.denimgroup.threadfix.service.AstamAttackSurfaceService;
import com.secdec.astam.common.data.models.Attacksurface;
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

    @Override
    public Attacksurface.EntryPointWebSet getEntryPointWebSet(AstamAttackSurfaceMapper mapper, int applicationId){
        List<WebAttackSurface> attackSurfaces = attackSurfaceDao.retrieveWebAttackSurfaceByAppId(mapper.getApplicationId());

        mapper.addWebEntryPoints(attackSurfaces);

        return mapper.getEntryPointwebSet();
    }

}
