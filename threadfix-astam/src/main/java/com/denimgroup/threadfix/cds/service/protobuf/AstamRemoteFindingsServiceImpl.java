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

import com.denimgroup.threadfix.cds.service.AstamRemoteFindingsService;
import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.VulnerabilityDao;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.data.entities.Vulnerability;
import com.denimgroup.threadfix.mapper.AstamFindingsMapper;
import com.secdec.astam.common.data.models.Entities;
import com.secdec.astam.common.data.models.Findings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//TODO: refactor duplicate code
@Service
public class AstamRemoteFindingsServiceImpl implements AstamRemoteFindingsService {
    private final VulnerabilityDao vulnerabilityDao;
    private final ScanDao scanDao;

    @Autowired
    public AstamRemoteFindingsServiceImpl(VulnerabilityDao vulnerabilityDao, ScanDao scanDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.scanDao = scanDao;
    }

    @Override
    public List<Scan> getScansByApplicationId(int applicationId) {
        List<Integer> applicationIdList = new ArrayList<Integer>();
        applicationIdList.add(applicationId);
        return scanDao.retrieveByApplicationIdList(applicationIdList);
    }

    @Override
    public Findings.DastFindingSet getDastFindings(AstamFindingsMapper astamMapper){
        List<Scan> scanList = getScansByApplicationId(astamMapper.getApplicationId());

        for (int i=0; i<scanList.size(); i++) {
            Scan scan = scanList.get(i);
            String scanType = scan.getScannerType();

            if (scanType.equals(Scan.DYNAMIC)) {
                astamMapper.addDastFindings(scan);
            }
        }

        return astamMapper.getDastFindingSet();
    }

    @Override
    public Findings.SastFindingSet getSastFindings(AstamFindingsMapper astamMapper) {
        List<Scan> scanList = getScansByApplicationId(astamMapper.getApplicationId());

        for (int i=0; i<scanList.size(); i++) {
            Scan scan = scanList.get(i);
            String scanType = scan.getScannerType();

            if (scanType.equals(Scan.STATIC)) {
                astamMapper.addSastFindings(scan);
            }
        }

        return astamMapper.getSastFindingSet();
    }

    @Override
    public Findings.CorrelatedFindingSet getCorrelatedFindings(AstamFindingsMapper astamMapper){
        List<Vulnerability> vulnerabilityList = vulnerabilityDao
                .retrieveAllByApplication(astamMapper.getApplicationId());
        astamMapper.addCorrelatedFindings(vulnerabilityList);

        return astamMapper.getCorrelatedFindingSet();
    }

    @Override
    public Entities.ExternalToolSet getExternalTools(AstamFindingsMapper astamMapper) {
        return astamMapper.getExternalToolsSet();
    }

}