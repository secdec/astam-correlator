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

    @Override
    public int compareTo(SourceCodeStatus o) {
        return this.getDateTimeChecked().compareTo(o.getDateTimeChecked());
    }
}
