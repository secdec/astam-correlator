package com.denimgroup.threadfix.data.entities.astam;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by jrios on 7/26/2017.
 */
@Entity
public class AstamApplicationEnvironment extends AstamAuditableEntity{


    @Column(length = 1024, nullable = false)
    @JsonView(Object.class)
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}
