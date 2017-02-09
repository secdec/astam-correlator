package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.VulnerabilityDao;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.data.entities.Vulnerability;
import com.denimgroup.threadfix.service.AstamFindingsService;
import com.denimgroup.threadfix.mapper.AstamFindingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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

    public File getFindings(int applicationId) {
        List<Vulnerability> vulnerabilityList = vulnerabilityDao.retrieveAllByApplication(applicationId);
        List<Integer> applicationIdList = new ArrayList<Integer>();
        applicationIdList.add(applicationId);
        List<Scan> scanList = scanDao.retrieveByApplicationIdList(applicationIdList);

        AstamFindingsMapper findings = new AstamFindingsMapper();

        for (int i=0; i<scanList.size(); i++) {
            Scan scan = scanList.get(i);
            String scanType = scan.getScannerType();

            if (scanType.equals(Scan.STATIC)) {
                findings.addSastFindings(scan);
            } else if (scanType.equals(Scan.DYNAMIC)) {
                findings.addDastFindings(scan);
            }
        }

        findings.addCorrelatedFindings(vulnerabilityList);

        return findings.getFindingsFile();
    }
}
