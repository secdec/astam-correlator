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

package com.denimgroup.threadfix.cds.rest;

import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.secdec.astam.common.data.models.Findings.*;

/**
 * Created by amohammed on 6/23/2017.
 */
public interface AstamFindingsClient {

    //findings/correlationResults
     RestResponse<CorrelationResultSet> getAllCorrelationResults();
     RestResponse createCorrelationResult(CorrelationResult correlationResult);

    //findings/correlationResults/{correlationResultId}
     RestResponse<CorrelationResult> getCorrelationResult(String correlationResultId);
     RestResponse updateCorrelationResult(String correlationResultId, CorrelationResult correlationResult);
     RestResponse deleteCorrelationResult(String correlationResultId);

    //findings//correlatedFindings
     RestResponse<CorrelatedFindingSet> getAllCorrelatedFindings();
     RestResponse createCorrelatedFinding(CorrelatedFinding correlatedFinding);

    //findings//correlatedFindings/{correlatedFindingId}
     RestResponse<CorrelatedFinding> getCorrelatedFinding(String correlatedFindingId);
     RestResponse updateCorrelatedFinding(String correlatedFindingId, CorrelatedFinding correlatedFinding);
     RestResponse deleteCorrelatedFinding(String correlatedFindingId);

    //findings/rawFindings
     RestResponse<RawFindingsSet> getAllRawFindings();
     RestResponse createRawFindings(RawFindings rawFindings);

    //findings/rawFindings/{rawFindingsId}
     RestResponse<RawFindings> getRawFindings(String rawFindingsId);
     RestResponse updateRawFindings(String rawFindingsId, RawFindings rawFindings);
     RestResponse deleteRawFindings(String rawFindingsId);

    //findings/rawFindings/sast
     RestResponse<SastFindingSet> getAllSastFindings();
     RestResponse createSastFinding(SastFinding sastFinding);

    //findings/rawFindings/sast/{sastFindingId}
     RestResponse<SastFinding> getSastFinding(String sastFindingId);
     RestResponse updateSastFinding(String sastFindingId, SastFinding sastFinding);
     RestResponse deleteSastFinding(String sastFindingId);

    //findings/rawFindings/dast
     RestResponse<DastFindingSet> getAllDastFindings();
     RestResponse createDastFinding(DastFinding dastFinding);

    //findings/rawFindings/dast/{dastFindingId}
     RestResponse<DastFinding> getDastFinding(String dastFindingId);
     RestResponse updateDastFinding(String dastFindingId, DastFinding dastFinding);
     RestResponse deletedastFinding(String dastFindingId);
}
