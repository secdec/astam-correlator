package com.denimgroup.threadfix.data.entities.astam;

import com.denimgroup.threadfix.data.entities.AuditableEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Created by jsemtner on 2/5/2017.
 */
@MappedSuperclass
public class AstamAuditableEntity extends AuditableEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
