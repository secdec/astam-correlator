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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SourceCodeStatus")
public class SourceCodeStatus extends BaseEntity  implements Comparable<SourceCodeStatus>  {

    private static final long serialVersionUID = 5256222431372241529L;

    private String commitId;
    private Application application;
    private Date dateTimeChecked;
    private SourceCodeRepoType repoType;
    private String branch;

    @Column(length = 40, nullable = false)
    @JsonView(Object.class)
    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @ManyToOne
    @JoinColumn(name = "application", nullable = false)
    @JsonIgnore
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Column(nullable = false)
    @JsonView(Object.class)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateTimeChecked() {
        return dateTimeChecked;
    }

    public void setDateTimeChecked(Date dateTimeChecked) {
        this.dateTimeChecked = dateTimeChecked;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(Object.class)
    public SourceCodeRepoType getRepoType() {return repoType;}
    public void setRepoType(SourceCodeRepoType repoType) { this.repoType = repoType;}


    @Column(length = 256, nullable = false)
    @JsonView(Object.class)
    public String getBranch() {return branch;}
    public void setBranch(String branch) {this.branch = branch;}


    @Override
    public int compareTo(SourceCodeStatus o) {
        return this.getDateTimeChecked().compareTo(o.getDateTimeChecked());
    }

    public String toString(){
        if(application != null && application.getName() != null) {
            return application.getId() + " " + application.getName() + " on " + getBranch() + " branch was last checked on "
                    + dateTimeChecked + " with a commitID of " + commitId;
        }
        else return "Invalid Source Code Status. Unable to toString().";
    }

}
