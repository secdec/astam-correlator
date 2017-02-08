package com.denimgroup.threadfix.data.entities.astam;

import com.denimgroup.threadfix.data.entities.AuditableEntity;

import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Created by jsemtner on 2/5/2017.
 */
@MappedSuperclass
public class AstamAuditableEntity extends AuditableEntity {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
