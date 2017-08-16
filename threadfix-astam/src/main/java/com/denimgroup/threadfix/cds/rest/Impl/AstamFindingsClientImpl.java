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

package com.denimgroup.threadfix.cds.rest.Impl;

import com.denimgroup.threadfix.cds.rest.AstamFindingsClient;
import com.denimgroup.threadfix.cds.rest.HttpMethods;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.secdec.astam.common.data.models.Findings.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;


/**
 * Created by amohammed on 6/23/2017.
 */

@Component
public class AstamFindingsClientImpl implements AstamFindingsClient {

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamFindingsClientImpl.class);

    @Autowired
    private HttpMethods httpUtils;

    private final static String CONTROLLER_FINDINGS = "findings/",
            CORRELATION_RESULTS = "correlationResults/",
            CORRELATED_FINDINGS = "correlatedFindings/",
            RAW_FINDINGS = "rawFindings/",
            SAST = "sast/",
            DAST = "dast/",
            EXCEPTION_MESSAGE = "InvalidProtocolBufferException while attempting to parse retrieved protobuf data.";

    public AstamFindingsClientImpl( ){
    }

    @Override
    public RestResponse<CorrelationResultSet> getAllCorrelationResults() {
        RestResponse<CorrelationResultSet> response = httpUtils.httpGet(
                CONTROLLER_FINDINGS + CORRELATION_RESULTS);
        if(response.success){
            try {
                response.object = CorrelationResultSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createCorrelationResult(@Nonnull CorrelationResult correlationResult) {
        byte[] entity = correlationResult.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_FINDINGS + CORRELATION_RESULTS, entity);
        return response;
    }

    @Override
    public RestResponse<CorrelationResult> getCorrelationResult(@Nonnull String correlationResultId) {
        RestResponse<CorrelationResult> response = httpUtils.httpGet(CONTROLLER_FINDINGS + CORRELATION_RESULTS,
                correlationResultId);
        if(response.success){
            try {
                response.object = CorrelationResult.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateCorrelationResult(@Nonnull String correlationResultId,
                                                @Nonnull CorrelationResult correlationResult) {
        byte[] entity = correlationResult.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_FINDINGS + CORRELATION_RESULTS,
                correlationResultId, entity);
        return response;
    }

    @Override
    public RestResponse deleteCorrelationResult(@Nonnull String correlationResultId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_FINDINGS + CORRELATION_RESULTS,
                correlationResultId);
        return response;
    }

    @Override
    public RestResponse<CorrelatedFindingSet> getAllCorrelatedFindings() {
        RestResponse<CorrelatedFindingSet> response = httpUtils.httpGet(CONTROLLER_FINDINGS + CORRELATED_FINDINGS);
        if(response.success){
            try {
                response.object = CorrelatedFindingSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createCorrelatedFinding(@Nonnull CorrelatedFinding correlatedFinding) {
        byte[] entity = correlatedFinding.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_FINDINGS + CORRELATED_FINDINGS, entity);
        return response;
    }

    @Override
    public RestResponse<CorrelatedFinding> getCorrelatedFinding(@Nonnull String correlatedFindingId) {
        RestResponse<CorrelatedFinding> response = httpUtils.httpGet(CONTROLLER_FINDINGS + CORRELATED_FINDINGS,
                correlatedFindingId);
        if(response.success){
            try {
                response.object = CorrelatedFinding.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateCorrelatedFinding(@Nonnull String correlatedFindingId,
                                                @Nonnull CorrelatedFinding correlatedFinding) {
        byte[] entity = correlatedFinding.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_FINDINGS + CORRELATED_FINDINGS,
                correlatedFindingId, entity);
        return response;
    }

    @Override
    public RestResponse deleteCorrelatedFinding(@Nonnull String correlatedFindingId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_FINDINGS + CORRELATED_FINDINGS ,
                correlatedFindingId);
        return response;
    }

    @Override
    public RestResponse<RawFindingsSet> getAllRawFindings() {
        RestResponse<RawFindingsSet> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS);
        if(response.success){
            try {
                response.object = RawFindingsSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createRawFindings(@Nonnull RawFindings rawFindings) {
        byte[] entity = rawFindings.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_FINDINGS + RAW_FINDINGS, entity);
        return response;
    }

    @Override
    public RestResponse<RawFindings> getRawFindings(@Nonnull String rawFindingsId) {
        RestResponse<RawFindings> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS);
        if(response.success){
            try {
                response.object = RawFindings.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateRawFindings(@Nonnull String rawFindingsId, @Nonnull RawFindings rawFindings) {
        byte[] entity = rawFindings.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_FINDINGS + RAW_FINDINGS, rawFindingsId, entity);
        return response;
    }

    @Override
    public RestResponse deleteRawFindings(@Nonnull String rawFindingsId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_FINDINGS + RAW_FINDINGS, rawFindingsId);
        return response;
    }

    @Override
    public RestResponse<SastFindingSet> getAllSastFindings() {
        RestResponse<SastFindingSet> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS + SAST);
        if(response.success){
            try {
                response.object = SastFindingSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createSastFinding(@Nonnull SastFinding sastFinding) {
        byte[] entity = sastFinding.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_FINDINGS + RAW_FINDINGS + SAST, entity);
        return response;
    }

    @Override
    public RestResponse<SastFinding> getSastFinding(@Nonnull String sastFindingId) {
        RestResponse<SastFinding> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS, sastFindingId);
        if(response.success){
            try {
                response.object = SastFinding.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateSastFinding(@Nonnull String sastFindingId, @Nonnull SastFinding sastFinding) {
        byte[] entity = sastFinding.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_FINDINGS + RAW_FINDINGS, sastFindingId, entity);
        return response;
    }

    @Override
    public RestResponse deleteSastFinding(@Nonnull String sastFindingId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_FINDINGS + RAW_FINDINGS, sastFindingId);
        return response;
    }

    @Override
    public RestResponse<DastFindingSet> getAllDastFindings() {
        RestResponse<DastFindingSet> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS + DAST);
        if(response.success){
            try {
                response.object = DastFindingSet.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse createDastFinding(@Nonnull DastFinding dastFinding) {
        byte[] entity = dastFinding.toByteArray();
        RestResponse response = httpUtils.httpPost(CONTROLLER_FINDINGS + RAW_FINDINGS + DAST, entity);
        return response;
    }

    @Override
    public RestResponse<DastFinding> getDastFinding(@Nonnull String dastFindingId) {
        RestResponse<DastFinding> response = httpUtils.httpGet(CONTROLLER_FINDINGS + RAW_FINDINGS, dastFindingId);
        if(response.success){
            try {
                response.object = DastFinding.parseFrom(response.data);
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error(EXCEPTION_MESSAGE, e);
            }
        }
        return response;
    }

    @Override
    public RestResponse updateDastFinding(@Nonnull String dastFindingId, DastFinding dastFinding) {
        byte[] entity = dastFinding.toByteArray();
        RestResponse response = httpUtils.httpPut(CONTROLLER_FINDINGS + RAW_FINDINGS, dastFindingId, entity);
        return response;
    }

    @Override
    public RestResponse deletedastFinding(@Nonnull String dastFindingId) {
        RestResponse response = httpUtils.httpDelete(CONTROLLER_FINDINGS + RAW_FINDINGS, dastFindingId);
        return response;
    }
}

