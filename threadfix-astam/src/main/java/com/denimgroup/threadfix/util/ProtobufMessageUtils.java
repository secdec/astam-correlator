package com.denimgroup.threadfix.util;

import com.denimgroup.threadfix.data.entities.AuditableEntity;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.secdec.astam.common.data.models.Common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by jsemtner on 2/12/2017.
 */
public class ProtobufMessageUtils {
    public static Common.UUID createUUID(String uuid) {
        return Common.UUID.newBuilder().setValue(uuid).build();
    }

    public static Timestamp createTimestamp(Date date) {
        return Timestamp.newBuilder().setSeconds(date.getTime()).build();
    }

    public static Common.URL createUrl(String strUrl) {
        return Common.URL.newBuilder().setValue(strUrl).build();
    }

    public static Common.RecordData createRecordData(AuditableEntity auditableEntity) {
        Common.RecordData recordData = Common.RecordData.newBuilder()
                .setCreatedTime(createTimestamp(auditableEntity.getCreatedDate()))
                .setEditedTime(createTimestamp(auditableEntity.getModifiedDate()))
                .setVersionId(createUUID(auditableEntity.getId().toString())).build();

        return recordData;
    }

    public static <T extends Message> void writeListToOutput(List<T> messageList, OutputStream output)
            throws IOException {
        for (int i=0; i<messageList.size(); i++) {
            messageList.get(i).writeTo(output);
        }
    }
}
