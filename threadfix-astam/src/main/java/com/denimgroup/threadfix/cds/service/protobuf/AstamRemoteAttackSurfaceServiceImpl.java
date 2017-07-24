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

package com.denimgroup.threadfix.cds.service.protobuf;

import com.denimgroup.threadfix.cds.service.AstamRemoteAttackSurfaceService;
import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.secdec.astam.common.data.models.Attacksurface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AstamRemoteAttackSurfaceServiceImpl implements AstamRemoteAttackSurfaceService {
    private final WebAttackSurfaceDao attackSurfaceDao;

    @Autowired
    public AstamRemoteAttackSurfaceServiceImpl(WebAttackSurfaceDao attackSurfaceDao, ScanDao scanDao) {
        this.attackSurfaceDao = attackSurfaceDao;
    }

    @Override
    public Attacksurface.EntryPointWebSet getEntryPointWebSet(AstamAttackSurfaceMapper mapper, int applicationId){
        List<WebAttackSurface> attackSurfaces = attackSurfaceDao.retrieveWebAttackSurfaceByAppId(applicationId);

        mapper.addWebEntryPoints(attackSurfaces);

        return mapper.getEntryPointwebSet();
    }

}
