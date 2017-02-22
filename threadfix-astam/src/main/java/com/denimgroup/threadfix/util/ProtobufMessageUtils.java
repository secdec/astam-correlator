package com.denimgroup.threadfix.util;

import com.denimgroup.threadfix.data.entities.AuditableEntity;
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

    public static Common.RecordData createRecordData(AuditableEntity auditableEntity) {
        Common.RecordData recordData = Common.RecordData.newBuilder()
                .setCreatedTime(createTimestamp(auditableEntity.getCreatedDate()))
                .setEditedTime(createTimestamp(auditableEntity.getModifiedDate()))
                .setVersionId(createUUIDFromInt(auditableEntity.getId())).build();

        return recordData;
    }
}
