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

package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.mapper.AstamFindingsMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface AstamFindingsService {
    void writeDastFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeSastFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeCorrelatedFindingsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;
    void writeExternalToolsToOutput(AstamFindingsMapper mapper, OutputStream outputStream)
            throws IOException;

    /*Findings.DastFindingSet getDastFindingSet();
    Findings.SastFindingSet getSastFindingSet();
    Findings.CorrelatedFindingSet getCorrelatedFindingSet();*/

}
