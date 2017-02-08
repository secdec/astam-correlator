package com.denimgroup.threadfix.mapper;

import com.denimgroup.threadfix.data.entities.*;
import com.google.protobuf.Message;
import com.secdec.astam.common.data.models.Common;
import com.secdec.astam.common.data.models.Entities;
import com.secdec.astam.common.data.models.Findings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by jsemtner on 2/7/2017.
 */
public class FindingsMapper {
    private List<Entities.CWE> cwes;
    private List<Entities.ExternalTool> externalTools;

    private List<Findings.DastFinding> dastFindings;
    private List<Findings.SastFinding> sastFindings;
    private List<Findings.CorrelatedFinding> correlatedFindings;

    private Map<String, Common.Severity> severityMap;

    public FindingsMapper() {
        cwes = new ArrayList<Entities.CWE>();
        externalTools = new ArrayList<Entities.ExternalTool>();

        dastFindings = new ArrayList<Findings.DastFinding>();
        sastFindings = new ArrayList<Findings.SastFinding>();
        correlatedFindings = new ArrayList<Findings.CorrelatedFinding>();

        severityMap = new HashMap<String, Common.Severity>();
        severityMap.put(GenericSeverity.INFO,Common.Severity.INFO_SEVERITY);
        severityMap.put(GenericSeverity.LOW,Common.Severity.LOW_SEVERITY);
        severityMap.put(GenericSeverity.MEDIUM,Common.Severity.MEDIUM_SEVERITY);
        severityMap.put(GenericSeverity.HIGH,Common.Severity.HIGH_SEVERITY);
        severityMap.put(GenericSeverity.CRITICAL,Common.Severity.CRITICAL_SEVERITY);
    }

    private Common.UUID createUUID(String uuid) {
        return Common.UUID.newBuilder().setValue(uuid.toString()).build();
    }

    public Entities.CWE addCwe(GenericVulnerability genericVulnerability) {
        Entities.CWE cwe = Entities.CWE.newBuilder()
                .setWeaknessId(genericVulnerability.getCweId())
                .setTitle(genericVulnerability.getName()).build();

        if (!cwes.contains(cwe)) {
            cwes.add(cwe);
        }

        return cwe;
    }

    public Entities.ExternalTool addExternalTool(ChannelType channelType) {
        Entities.ExternalTool externalTool = Entities.ExternalTool.newBuilder()
                .setId(createUUID(channelType.getId().toString()))
                .setName(channelType.getName()).build();

        if (!externalTools.contains(externalTool)) {
            externalTools.add(externalTool);
        }

        return externalTool;
    }

    private Common.HttpMethod getHttpMethod(String httpMethod) {
        return Common.HttpMethod.valueOf(httpMethod);
    }

    private Findings.DastFinding.AttackVariant getAttackVariant(Finding finding) {
        SurfaceLocation surfaceLocation = finding.getSurfaceLocation();
        String attackRequest = finding.getAttackRequest();

        Findings.DastFinding.AttackVariant.WebAttackIteration webAttackIteration =
                Findings.DastFinding.AttackVariant.WebAttackIteration.newBuilder()
                        .setResourcePath(surfaceLocation.getPath())
                        .setRequestMethod(getHttpMethod(surfaceLocation.getHttpMethod()))
                        // TODO: Add .putUrlParameters(parseUrlParameters(attackRequest)
                        // TODO: Add .putHeaders(parseHeaders(attackRequest)
                        // TODO: Add .setPostData(parseBody(attackRequest))
                        .setAttackResponseString(finding.getAttackResponse()).build();

        Findings.DastFinding.AttackVariant.AttackStep attackStep =
                Findings.DastFinding.AttackVariant.AttackStep.newBuilder()
                        .setWebAttackStep(webAttackIteration).build();

        return Findings.DastFinding.AttackVariant.newBuilder().addAttackSteps(attackStep).build();
    }

    public void addDastFindings(Scan scan) {
        List<Finding> findings = scan.getFindings();
        Entities.ExternalTool externalTool = addExternalTool(scan.getApplicationChannel().getChannelType());

        for (int i=0; i<findings.size(); i++) {
            Finding finding = findings.get(i);
            GenericVulnerability genericVulnerability = finding.getVulnerability().getGenericVulnerability();

            Findings.DastFinding dastFinding = Findings.DastFinding.newBuilder()
                    .setName(genericVulnerability.getName())
                    .addCwe(genericVulnerability.getCweId())
                    .setDescription(finding.getLongDescription())
                    .setReportingTool(externalTool)
                    .setToolDefinedSeverity(finding.getChannelSeverity().getName())
                    .addAttackVariants(getAttackVariant(finding))
                    .setId(createUUID(finding.getUuid())).build();

            dastFindings.add(dastFinding);
        }
    }

    private List<Findings.SastFinding.TraceNode> getTraceNodes(Finding finding) {
        List<DataFlowElement> dataFlowElements = finding.getDataFlowElements();
        List<Findings.SastFinding.TraceNode> traceNodeList = new ArrayList<Findings.SastFinding.TraceNode>();
        for (int i=0; i<dataFlowElements.size(); i++) {
            DataFlowElement dataFlowElement = dataFlowElements.get(i);
            Findings.SastFinding.TraceNode traceNode = Findings.SastFinding.TraceNode.newBuilder()
                    .setColumn(dataFlowElement.getColumnNumber())
                    .setLine(dataFlowElement.getLineNumber())
                    .setFile(dataFlowElement.getSourceFileName())
                    .setLineOfCode(dataFlowElement.getLineText()).build();

            traceNodeList.add(traceNode);
        }

        return traceNodeList;
    }

    public void addSastFindings(Scan scan) {
        List<Finding> findings = scan.getFindings();
        Entities.ExternalTool externalTool = addExternalTool(scan.getApplicationChannel().getChannelType());

        for (int i=0; i<findings.size(); i++) {
            Finding finding = findings.get(i);
            GenericVulnerability genericVulnerability = finding.getVulnerability().getGenericVulnerability();
            Entities.CWE cwe = addCwe(genericVulnerability);

            Findings.SastFinding sastFinding = Findings.SastFinding.newBuilder()
                    .setName(genericVulnerability.getName())
                    .addCwe(cwe)
                    .setDescription(finding.getLongDescription())
                    .setReportingTool(externalTool)
                    .setToolDefinedSeverity(finding.getChannelSeverity().getName())
                    .addAllTrace(getTraceNodes(finding))
                    .setId(createUUID(finding.getId().toString())).build();

            sastFindings.add(sastFinding);
        }
    }

    private List<Common.UUID> getUuidsForFindings(List<Finding> findingList) {
        List<Common.UUID> uuids = new ArrayList<Common.UUID>();

        for (int i=0; i<findingList.size(); i++) {
            Finding finding = findingList.get(i);
            uuids.add(createUUID(finding.getUuid()));
        }

        return uuids;
    }

    public void addCorrelatedFindings(List<Vulnerability> vulnerabilityList) {
        for (int i=0; i<vulnerabilityList.size(); i++) {
            Vulnerability vulnerability = vulnerabilityList.get(i);
            GenericVulnerability genericVulnerability = vulnerability.getGenericVulnerability();
            List<Finding> findings = vulnerability.getFindings();
            GenericSeverity severity = vulnerability.getGenericSeverity();

            if (findings.size() < 2) {
                continue;
            }

            Findings.CorrelatedFinding correlatedFinding = Findings.CorrelatedFinding.newBuilder()
                    .setName(genericVulnerability.getName())
                    .addAllDastFindingIds(getUuidsForFindings(vulnerability.getDynamicFindings()))
                    .addAllSastFindingIds(getUuidsForFindings(vulnerability.getStaticFindings()))
                    .setSeverity(severityMap.get(severity.getName()))
                    .addApplicableCwes(genericVulnerability.getCweId()).build();

            correlatedFindings.add(correlatedFinding);
        }
    }

    private static <T extends Message> void writeListToOutput(List<T> messageList, FileOutputStream output)
            throws IOException {
        for (int i=0; i<messageList.size(); i++) {
            messageList.get(i).writeTo(output);
        }
    }

    public File getFindingsFile() {
        try {
            File findingsFile = new File("data/findings.ser");
            findingsFile.createNewFile();

            FileOutputStream output = new FileOutputStream(findingsFile);
            writeListToOutput(cwes, output);
            writeListToOutput(externalTools, output);
            writeListToOutput(dastFindings, output);
            writeListToOutput(sastFindings, output);
            writeListToOutput(correlatedFindings, output);
            // TODO: add cweset, externaltoolset
            // TODO: add rawfindings, correlationresult, correlationresultset

            output.close();

            return findingsFile;
        } catch (FileNotFoundException ex) {
            // TODO: Handle file not found exception
        } catch (IOException ex) {
            // TODO: Handle IO exception
        }

        return null;
    }
}
