package com.denimgroup.threadfix.data.dao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;


public interface ScheduledGitPollDao extends ScheduledJobDao<ScheduledGitPoll> {
    ScheduledGitPoll loadByApplication(Application application);
}
