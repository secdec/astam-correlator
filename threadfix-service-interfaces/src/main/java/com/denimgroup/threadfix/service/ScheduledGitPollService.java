package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;

public interface ScheduledGitPollService extends ScheduledJobService<ScheduledGitPoll>{

    public ScheduledGitPoll loadByApplication(Application application);

    public void updatePoll(ScheduledGitPoll poll);

    public ScheduledGitPoll loadByApplicationOrDefault(Application application);


}
