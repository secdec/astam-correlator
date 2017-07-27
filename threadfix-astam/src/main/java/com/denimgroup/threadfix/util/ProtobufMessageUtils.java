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

package com.denimgroup.threadfix.util;

import com.denimgroup.threadfix.data.entities.astam.AstamAuditableEntity;
import com.denimgroup.threadfix.data.entities.astam.AstamBaseEntity;
import com.google.protobuf.Timestamp;
import com.secdec.astam.common.data.models.Common;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * Created by jsemtner on 2/12/2017.
 */
public class ProtobufMessageUtils {
    public static Common.UUID createUUID(String uuid) {
        return Common.UUID.newBuilder().setValue(uuid).build();
    }

    public static Timestamp createTimestamp(Date date) {
        Instant instant = date.toInstant();
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano()).build();
    }

    public static Common.URL createUrl(String strUrl) {
        return Common.URL.newBuilder().setValue(strUrl).build();
    }

    public static Common.UUID createUUIDFromInt(int id) {
        String paddedInt = String.format("%032d", id);
        try {
            MaskFormatter formatter = new MaskFormatter("########-####-####-####-############");
            formatter.setValueContainsLiteralCharacters(false);
            String uuid = formatter.valueToString(paddedInt);
            return Common.UUID.newBuilder().setValue(uuid).build();
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
    * when it's first pushed to the CDS, we need to update the original entity's UUID
     * with the uuid given in the response
    * @param uuid , this the uuid created with the local id. Example: 00000000-0000-0000-0000-0000048879
    * @return id , this will be 48879
    * */
    public static int createIdFromUUID(String uuid){
        //uuid = uuid.replace("-", "0");
        int id = Integer.valueOf(uuid);
        return id;
    }

    public static Common.RecordData createRecordData(AstamAuditableEntity astamAuditableEntity) {
        Common.RecordData.Builder recordDataBuilder = Common.RecordData.newBuilder()
                .setCreatedTime(createTimestamp(astamAuditableEntity.getCreatedDate()))
                .setEditedTime(createTimestamp(astamAuditableEntity.getModifiedDate()))
                .setVersionId(createUUID(astamAuditableEntity));

        return recordDataBuilder.build();
    }

    public static Common.UUID createUUID(AstamAuditableEntity astamAuditableEntity){
        if(astamAuditableEntity.getUuid() != null && !astamAuditableEntity.getUuid().isEmpty()){
            return createUUID(astamAuditableEntity.getUuid());
        } else {
            return createUUIDFromInt(astamAuditableEntity.getId());
        }
    }

    public static Common.UUID createUUID(AstamBaseEntity astamBaseEntity){
        if(astamBaseEntity.getUuid() != null && !astamBaseEntity.getUuid().isEmpty()){
            return createUUID(astamBaseEntity.getUuid());
        } else {
            return createUUIDFromInt(astamBaseEntity.getId());
        }
    }

}
