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
package com.denimgroup.threadfix.cds.service.protobuf;

import com.denimgroup.threadfix.cds.service.AstamRemoteAttackSurfaceService;
import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.WebAttackSurfaceDao;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import com.denimgroup.threadfix.data.entities.astam.AstamRawDiscoveredAttackSurface;
import com.denimgroup.threadfix.mapper.AstamAttackSurfaceMapper;
import com.secdec.astam.common.data.models.Attacksurface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AstamRemoteAttackSurfaceServiceImpl implements AstamRemoteAttackSurfaceService {

    private final WebAttackSurfaceDao attackSurfaceDao;

    private AstamAttackSurfaceMapper mapper;

    private List<WebAttackSurface> attackSurfaces;

    @Autowired
    public AstamRemoteAttackSurfaceServiceImpl(WebAttackSurfaceDao attackSurfaceDao, ScanDao scanDao) {
        this.attackSurfaceDao = attackSurfaceDao;
    }

    public void setup(int applicationId){
        attackSurfaces = attackSurfaceDao.retrieveWebAttackSurfaceByAppId(applicationId);
        mapper = new AstamAttackSurfaceMapper(applicationId);
    }

    @Override
    public Attacksurface.RawDiscoveredAttackSurface getRawDiscoveredAttackSurface(){
        //TODO: change this
        //send this first, we must check if no attack surface has been found
        WebAttackSurface webAttackSurface = null;
        if(attackSurfaces != null && !attackSurfaces.isEmpty()){
            webAttackSurface = attackSurfaces.get(0);
        }

        //TODO: change this. Get RawDiscoverAttackSurface from the current deployment
       AstamRawDiscoveredAttackSurface rawDiscoveredAttackSurface = webAttackSurface.getAstamRawDiscoveredAttackSurface();
        mapper.createRawDiscoveredAttackSurface(rawDiscoveredAttackSurface);
        return mapper.getRawDiscoveredAttackSurface();
    }

    @Override
    public Attacksurface.EntryPointWebSet getEntryPointWebSet(){
        mapper.addWebEntryPoints(attackSurfaces);
        return mapper.getEntryPointwebSet();
    }



}
