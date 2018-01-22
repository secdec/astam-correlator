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
import com.denimgroup.threadfix.data.dao.VulnerabilityDao;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.data.entities.Vulnerability;
import com.denimgroup.threadfix.mapper.AstamFindingsMapper;
import com.denimgroup.threadfix.service.AstamFindingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsemtner on 2/3/2017.
 */
@Service
public class AstamFindingsServiceImpl implements AstamFindingsService {
    private final VulnerabilityDao vulnerabilityDao;
    private final ScanDao scanDao;

    @Autowired
    public AstamFindingsServiceImpl(VulnerabilityDao vulnerabilityDao, ScanDao scanDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.scanDao = scanDao;
    }

    private List<Scan> getScansByApplicationId(int applicationId) {
        List<Integer> applicationIdList = new ArrayList<Integer>();
        applicationIdList.add(applicationId);
        return scanDao.retrieveByApplicationIdList(applicationIdList);
    }

    @Override
    public void writeDastFindingsToOutput(AstamFindingsMapper astamMapper, OutputStream outputStream)
            throws IOException {
        List<Scan> scanList = getScansByApplicationId(astamMapper.getApplicationId());

        for (int i=0; i<scanList.size(); i++) {
            Scan scan = scanList.get(i);
            String scanType = scan.getScannerType();

            if (scanType.equals(Scan.DYNAMIC)) {
                astamMapper.addDastFindings(scan);
            }
        }

        astamMapper.writeDastFindingsToOutput(outputStream);
    }

    @Override
    public void writeSastFindingsToOutput(AstamFindingsMapper astamMapper, OutputStream outputStream)
            throws IOException {
        List<Scan> scanList = getScansByApplicationId(astamMapper.getApplicationId());

        for (int i=0; i<scanList.size(); i++) {
            Scan scan = scanList.get(i);
            String scanType = scan.getScannerType();

            if (scanType.equals(Scan.STATIC)) {
                astamMapper.addSastFindings(scan);
            }
        }

        astamMapper.writeSastFindingsToOutput(outputStream);
    }

    @Override
    public void writeCorrelatedFindingsToOutput(AstamFindingsMapper astamMapper,
                                                OutputStream outputStream) throws IOException {
        List<Vulnerability> vulnerabilityList = vulnerabilityDao
                .retrieveAllByApplication(astamMapper.getApplicationId());
        astamMapper.addCorrelatedFindings(vulnerabilityList);

        astamMapper.writeCorrelatedFindingsToOutput(outputStream);
    }

    @Override
    public void writeExternalToolsToOutput(AstamFindingsMapper astamMapper, OutputStream outputStream)
            throws IOException {
        astamMapper.writeExternalToolsToOutput(outputStream);
    }

}