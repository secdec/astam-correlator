package com.denimgroup.threadfix.mapper;


import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AstamAttackSurfaceMapper {
    private int applicationId;
    public List<Attacksurface.EntryPointWeb> webEntryPoints;
    public List<Attacksurface.EntryPointMobile> mobileEntryPoints;

    public AstamAttackSurfaceMapper(int applicationId) {
        this.applicationId = applicationId;
        this.webEntryPoints = new ArrayList<Attacksurface.EntryPointWeb>();
        this.mobileEntryPoints = new ArrayList<Attacksurface.EntryPointMobile>();
    }

    // TODO mobileEntryPoints when proto file is updated

    public int getApplicationId() {
        return applicationId;
    }

    public void addWebEntryPoints(List<Finding> findings) {
        for (Finding finding : findings) {

            Attacksurface.EntryPointWeb.Builder entryPointWebBuilder = Attacksurface.EntryPointWeb.newBuilder()
                    .addAllKnownAttackMechanisms(getAttackMechanisms(finding))
                    .addAllHttpMethod(getHttpMethods(finding.getSurfaceLocation()))
                    .setRelativePath(finding.getCalculatedUrlPath())
                    .setId(ProtobufMessageUtils.createUUID(finding.getId().toString()));

            if (!finding.getDataFlowElements().isEmpty())
                entryPointWebBuilder.setTrace(getTraceNode(finding));

            webEntryPoints.add(entryPointWebBuilder.build());
        }
    }

    private List<Attacksurface.EntryPointWeb.AttackMechanism> getAttackMechanisms(Finding finding) {
        List<Attacksurface.EntryPointWeb.AttackMechanism> attackMechanismList = new ArrayList<Attacksurface.EntryPointWeb.AttackMechanism>();
        SurfaceLocation surfaceLocation = finding.getSurfaceLocation();

        Attacksurface.EntryPointWeb.AttackMechanism attackMechanism = Attacksurface.EntryPointWeb.AttackMechanism.newBuilder()
                    .setType(getWebAttackMechanismType(surfaceLocation))
                    .setName(getAttackMechanismName(surfaceLocation))
                    .build();

        attackMechanismList.add(attackMechanism);

        return attackMechanismList;
    }

    private Common.WebAttackMechanismType getWebAttackMechanismType(SurfaceLocation location) {
        // TODO expand for Cookie and Header type assignments
        if (location.getParameter() != null)
            return Common.WebAttackMechanismType.PARAMETER;

        return Common.WebAttackMechanismType.UNDEFINED_WEBENTRYPOINT;
    }

    private String getAttackMechanismName(SurfaceLocation location) {
        // TODO expand for Cookie and Header type assignments
        if (location.getParameter() != null)
            return location.getParameter();

        return "";
    }

    private List<Common.HttpMethod> getHttpMethods(SurfaceLocation surfaceLocation) {
        List<Common.HttpMethod> httpMethods = new ArrayList<Common.HttpMethod>();

        if (surfaceLocation.getHttpMethod() != null)
             httpMethods.add(Common.HttpMethod.valueOf(surfaceLocation.getHttpMethod()));

        return httpMethods;
    }

    private Entities.TraceNode getTraceNode(Finding finding) {
        DataFlowElement first = finding.getDataFlowElements().get(0);

        Entities.TraceNode traceNode = Entities.TraceNode.newBuilder()
                .setFile(first.getSourceFileName())
                .setLine(first.getLineNumber())
                .setColumn(first.getColumnNumber())
                .setLineOfCode(first.getLineText())
                .build();

        return traceNode;
    }

    private Attacksurface.RawDiscoveredAttackSurface createRawDiscoveredAttackSurface() {
        List<Common.UUID> webEntryPointIds = new ArrayList<Common.UUID>();
        for (Attacksurface.EntryPointWeb entryPointWeb : webEntryPoints)
            webEntryPointIds.add(entryPointWeb.getId());

        List<Common.UUID> mobileEntryPointIds = new ArrayList<Common.UUID>();
        for (Attacksurface.EntryPointMobile entryPointMobile : mobileEntryPoints)
            mobileEntryPointIds.add(entryPointMobile.getId());

        Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface = Attacksurface.RawDiscoveredAttackSurface.newBuilder()
                .addAllEntryPointWebIds(webEntryPointIds)
                .addAllEntryPointMobileIds(mobileEntryPointIds)
                //.setReportingTool(service.findIdByName(ThreadFix)
                .build();

        return rawDiscoveredAttackSurface;
    }

    public void writeAttackSurfaceToOutput(OutputStream outputStream) throws IOException {
        Attacksurface.EntryPointWebSet entryPointWebSet = Attacksurface.EntryPointWebSet.newBuilder()
                .addAllWebEntryPoints(webEntryPoints)
                .build();

        entryPointWebSet.writeTo(outputStream);
    }
}
