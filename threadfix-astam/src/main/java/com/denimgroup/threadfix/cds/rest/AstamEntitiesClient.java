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
import com.secdec.astam.common.data.models.Entities;

/**
 * Created by amohammed on 7/11/2017.
 */
public interface AstamEntitiesClient {
    //entities

    //entities/CWE
    RestResponse<Entities.CWESet> getAllCWEs();
    RestResponse createCWE(Entities.CWE cwe);

    //entities/cwe/[cweId]
    RestResponse<Entities.CWE> getCWE(String cweIdParam);
    RestResponse updateCWE(String cweIdParam, Entities.CWE cwe);

    //entities/CAPEC
    RestResponse<Entities.CAPECSet> getAllCAPECs();
    RestResponse createCAPEC(Entities.CAPEC capec);

    //entities/CAPEC/{capecId}
    RestResponse<Entities.CAPEC> getCAPEC(String capecIdParam);
    RestResponse updateCAPEC(String capecIdParam, Entities.CAPEC capec);

    //entities/externalTool
    RestResponse<Entities.ExternalToolSet> getAllExternalTools();
    RestResponse createExternalTool(Entities.ExternalTool externalTool);

    //entities/externalTool/{ExternalToolId}
    RestResponse<Entities.ExternalTool> getExternalTool(String externalToolIdParam);
    RestResponse updateExternalTool(String externalToolIdParam, Entities.ExternalTool externalTool);
    RestResponse deleteExternalTool(String externalToolIdParam);
}
