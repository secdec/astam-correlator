package com.denimgroup.threadfix.service.protobuf;

import com.denimgroup.threadfix.data.dao.ApplicationDao;
import com.denimgroup.threadfix.data.dao.ScanDao;
import com.denimgroup.threadfix.data.dao.VulnerabilityDao;
import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.service.FindingsService;
import com.secdec.astam.common.data.models.Common;
import com.secdec.astam.common.data.models.Entities;
import com.secdec.astam.common.data.models.Findings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsemtner on 2/3/2017.
 */
@Service
public class FindingsServiceImpl implements FindingsService {
    private final VulnerabilityDao vulnerabilityDao;
    private final ApplicationDao applicationDao;
    private final ScanDao scanDao;

    @Autowired
    public FindingsServiceImpl(VulnerabilityDao vulnerabilityDao, ApplicationDao applicationDao, ScanDao scanDao) {
        this.vulnerabilityDao = vulnerabilityDao;
        this.applicationDao = applicationDao;
        this.scanDao = scanDao;
    }

    @Override
    public Findings.RawFindings getFindings(int applicationId) {
        List<Vulnerability> vulnerabilityList = vulnerabilityDao.retrieveAllByApplication(applicationId);

        List<Findings.SastFinding> sastFindings = new ArrayList<Findings.SastFinding>();
        List<Findings.DastFinding> dastFindings = new ArrayList<Findings.DastFinding>();
        List<Findings.CorrelatedFinding> correlatedFindings = new ArrayList<Findings.CorrelatedFinding>();

        for (int i=0; i<vulnerabilityList.size(); i++) {
            Vulnerability vuln = vulnerabilityList.get(i);
            List<Finding> staticFindings = vuln.getStaticFindings();
            List<Finding> dynamicFindings = vuln.getDynamicFindings();

            // Create new SastFindings
            for (int j=0; j<staticFindings.size(); j++) {
                Finding sast = staticFindings.get(j);
                GenericVulnerability genericVuln = vuln.getGenericVulnerability();

                Entities.CWE cwe = Entities.CWE.newBuilder()
                        .setWeaknessId(genericVuln.getCweId())
                        .setTitle(genericVuln.getName()).build();

                Common.UUID uuid = Common.UUID.newBuilder()
                        .setValue(sast.getUuid().toString()).build();

                List<DataFlowElement> dataFlowElements = sast.getDataFlowElements();
                List<Findings.SastFinding.TraceNode> traceNodeList = new ArrayList<Findings.SastFinding.TraceNode>();
                for (int l=0; l<dataFlowElements.size(); l++) {
                    DataFlowElement dataFlowElement = dataFlowElements.get(l);
                    Findings.SastFinding.TraceNode traceNode = Findings.SastFinding.TraceNode.newBuilder()
                            .setColumn(dataFlowElement.getColumnNumber())
                            .setLine(dataFlowElement.getLineNumber())
                            .setFile(dataFlowElement.getSourceFileName())
                            .setLineOfCode(dataFlowElement.getLineText()).build();

                    traceNodeList.add(traceNode);
                }

                Findings.SastFinding.Builder sastBuilder = Findings.SastFinding.newBuilder()
                        .setName(genericVuln.getName())
                        .setCwe(0, cwe)
                        .setDescription(sast.getLongDescription())
                        .setId(uuid);

                for (int m=0; m<traceNodeList.size(); m++) {
                    sastBuilder.setTrace(m, traceNodeList.get(m));
                }

                sastFindings.add(sastBuilder.build());
            }

            // Create new DastFindings
            for (int k=0; k<dynamicFindings.size(); k++) {
                Finding dast = staticFindings.get(k);
            }

            // If more than one Sast and DastFinding, reate new CorrelatedFinding
        }

/*        Application app = applicationDao.retrieveById(applicationId);
        List<Scan> scans = app.getScans();

        for (int i=0; i<scans.size(); i++) {
            Scan scan = scans.get(i);
            String scanType = scan.getScannerType();
            if (scanType.equals("Dynamic")) {
                // Add Raw DAST finding
            } else if (scanType.equals("Static")) {
                // Add Raw SAST finding
            }
        }*/

        //Findings.RawFindings rawFindings = Findings.RawFindings.newBuilder()
        //        .setDastFindingIds();

        return null;
    }
}
