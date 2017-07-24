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

package com.denimgroup.threadfix.mapper;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ApplicationChannel;
import com.denimgroup.threadfix.data.entities.ChannelType;
import com.denimgroup.threadfix.util.ProtobufMessageUtils;
import com.secdec.astam.common.data.models.Entities;

import java.util.List;

/**
 * Created by amohammed on 7/12/2017.
 */
//TODO: refactor duplicate code
public class AstamEntitiesMapper {

    private List<Entities.ExternalTool> externalTools = null;

    public Entities.ExternalToolSet getExternalToolSet(Application app){
        List<ApplicationChannel> applicationChannels = app.getChannelList();
        for (ApplicationChannel applicationChannel : applicationChannels){
            addExternalTool(applicationChannel.getChannelType());
        }

        Entities.ExternalToolSet externalToolSet = Entities.ExternalToolSet.newBuilder()
                .addAllExternalTools(externalTools).build();

        return externalToolSet;
    }

    private Entities.ExternalTool addExternalTool(ChannelType channelType) {
        Entities.ExternalTool externalTool = Entities.ExternalTool.newBuilder()
                .setId(ProtobufMessageUtils.createUUID(channelType))
                .setToolName(channelType.getName()).build();

        if (!externalTools.contains(externalTool)) {
            externalTools.add(externalTool);
        }

        return externalTool;
    }

}
