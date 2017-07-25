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
package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.views.AllViews;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Created by amohammed on 7/23/2017.
 */

@Entity
@Table(name="AstamConfiguration")
public class AstamConfiguration extends BaseEntity {

    private static final long serialVersionUID = -1071749194635845799L;

    private String cdsApiUrl;
    private String cdsCompId;
    private String cdsBrokerUrl;

    private boolean hasConfiguration;

    public static AstamConfiguration getInitialConfig(){
        AstamConfiguration config = new AstamConfiguration();
        config.setHasConfiguration(false);
        config.setCdsApiUrl("");
        config.setCdsBrokerUrl("");
        config.setCdsCompId("");
        return config;
    }


    @JsonView(AllViews.FormInfo.class)
    @Column(length = 50, nullable = false)
    public String getCdsCompId(){return cdsCompId;}

    public void setCdsCompId(String cdsCompId) {this.cdsCompId = cdsCompId;}

    @JsonView(AllViews.FormInfo.class)
    @Column(length = 1024, nullable = false)
    public String getCdsApiUrl(){return cdsApiUrl;}

    public void setCdsApiUrl(String cdsApiUrl) {this.cdsApiUrl = cdsApiUrl;}

    @JsonView(AllViews.FormInfo.class)
    @Column(length = 1024, nullable = false)
    public String getCdsBrokerUrl(){return cdsBrokerUrl;}

    public void setCdsBrokerUrl(String cdsBrokerUrl) {this.cdsBrokerUrl = cdsBrokerUrl;}

   @Column
    public boolean getHasConfiguration() {
        return hasConfiguration;
    }

    public void setHasConfiguration(boolean hasConfiguration) {
        this.hasConfiguration = hasConfiguration;
    }
}
