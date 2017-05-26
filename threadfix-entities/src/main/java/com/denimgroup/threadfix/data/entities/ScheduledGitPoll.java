package com.denimgroup.threadfix.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
@Table(name="ScheduledGitPoll")
public class ScheduledGitPoll extends ScheduledJob {

    private static final long serialVersionUID = 1126471431354849432L;
    private Application application;
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "applicationId")
    @JsonIgnore
    public Application getApplication() {
        return this.application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
