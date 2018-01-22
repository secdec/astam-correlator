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
package com.denimgroup.threadfix.mapper;


import com.denimgroup.threadfix.data.entities.DataFlowElement;
import com.denimgroup.threadfix.data.entities.SurfaceLocation;
import com.denimgroup.threadfix.data.entities.WebAttackSurface;
import com.denimgroup.threadfix.data.entities.astam.AstamRawDiscoveredAttackSurface;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Attacksurface;
import com.secdec.astam.common.data.models.Common;
import com.secdec.astam.common.data.models.Entities;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AstamAttackSurfaceMapper {
    private int applicationId;
    public List<Attacksurface.EntryPointWeb> webEntryPoints;
    public List<Attacksurface.EntryPointMobile> mobileEntryPoints;
    public Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface;

    public AstamAttackSurfaceMapper(int applicationId) {
        this.applicationId = applicationId;
        this.webEntryPoints = new ArrayList<>();
        this.mobileEntryPoints = new ArrayList<>();
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void addWebEntryPoints(List<WebAttackSurface> attackSurfaces) {

        for (WebAttackSurface attackSurface : attackSurfaces) {
            //TODO: change this relationship
            Common.UUID rawDiscoveredAttackSurfaceId = ProtobufMessageUtils.createUUID(attackSurface.getAstamRawDiscoveredAttackSurface());
            Attacksurface.EntryPointWeb.Builder entryPointWebBuilder = Attacksurface.EntryPointWeb.newBuilder()
                    .setRecordData(ProtobufMessageUtils.createRecordData(attackSurface))
                    .addAllKnownAttackMechanisms(getAttackMechanisms(attackSurface))
                    .setTrace(getTraceNode(attackSurface))
                    .addAllHttpMethod(getHttpMethods(attackSurface.getSurfaceLocation()))
                    .setRelativePath(attackSurface.getSurfaceLocation().getPath())
                    .setRawDiscoveredAttackSurfaceId(rawDiscoveredAttackSurfaceId)
                    .setId(ProtobufMessageUtils.createUUID(attackSurface));

            webEntryPoints.add(entryPointWebBuilder.build());
        }
    }

    private List<Attacksurface.EntryPointWeb.AttackMechanism> getAttackMechanisms(WebAttackSurface attackSurface) {
        List<Attacksurface.EntryPointWeb.AttackMechanism> attackMechanismList = new ArrayList<>();
        SurfaceLocation surfaceLocation = attackSurface.getSurfaceLocation();

        Attacksurface.EntryPointWeb.AttackMechanism attackMechanism = Attacksurface.EntryPointWeb.AttackMechanism.newBuilder()
                .setType(getWebAttackMechanismType(surfaceLocation))
                .setName(getAttackMechanismName(surfaceLocation))
                //.setValueType() This is the parameter type String/Integer
                //.addAllValues() This maps to: "repeated string values = 4; "
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

    private Entities.TraceNode getTraceNode(WebAttackSurface attackSurface) {
        DataFlowElement element = attackSurface.getDataFlowElement();

        Entities.TraceNode.Builder traceNodeBuilder = Entities.TraceNode.newBuilder()
                .setLine(element.getLineNumber())
                .setColumn(element.getColumnNumber());

        String sourceFileName = element.getSourceFileName();
        if (sourceFileName != null) {
            traceNodeBuilder.setFile(sourceFileName);
        }

        String lineText = element.getLineText();
        if (lineText != null) {
            traceNodeBuilder.setLineOfCode(lineText);
        }

        return traceNodeBuilder.build();
    }

    public Attacksurface.RawDiscoveredAttackSurface createRawDiscoveredAttackSurface(AstamRawDiscoveredAttackSurface astamRawDiscoveredAttackSurface) {
        List<Common.UUID> webEntryPointIds = new ArrayList<Common.UUID>();
        for (Attacksurface.EntryPointWeb entryPointWeb : webEntryPoints)
            webEntryPointIds.add(entryPointWeb.getId());

        List<Common.UUID> mobileEntryPointIds = new ArrayList<Common.UUID>();
        for (Attacksurface.EntryPointMobile entryPointMobile : mobileEntryPoints)
            mobileEntryPointIds.add(entryPointMobile.getId());

        Common.UUID appDeploymentId = ProtobufMessageUtils.createUUID(astamRawDiscoveredAttackSurface.getAstamApplicationDeployment());
        Attacksurface.RawDiscoveredAttackSurface rawDiscoveredAttackSurface = Attacksurface.RawDiscoveredAttackSurface.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(astamRawDiscoveredAttackSurface))
                .setApplicationDeploymentId(appDeploymentId)
                //.setReportingExternalToolId() //this is threadfix
                //.addAllEntryPointWebIds(webEntryPointIds) //  these will be ignored as they are projections
                //.addAllEntryPointMobileIds(mobileEntryPointIds)
                //.setReportingTool(service.findIdByName(ThreadFix)
                .build();

        return rawDiscoveredAttackSurface;
    }

    public void writeAttackSurfaceToOutput(OutputStream outputStream) throws IOException {
        Attacksurface.EntryPointWebSet entryPointWebSet = getEntryPointwebSet();
        entryPointWebSet.writeTo(outputStream);
    }

    public Attacksurface.RawDiscoveredAttackSurface getRawDiscoveredAttackSurface(){
        return rawDiscoveredAttackSurface;
    }

    public Attacksurface.EntryPointWebSet getEntryPointwebSet(){
        Attacksurface.EntryPointWebSet  entryPointWebSet = Attacksurface.EntryPointWebSet.newBuilder()
                .addAllWebEntryPoints(webEntryPoints)
                .build();
        return entryPointWebSet;
    }

}
