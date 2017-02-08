package com.denimgroup.threadfix.data.entities.astam;

import com.denimgroup.threadfix.data.entities.BaseEntity;

import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Created by jsemtner on 2/5/2017.
 */
@MappedSuperclass
public class AstamBaseEntity extends BaseEntity{
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
