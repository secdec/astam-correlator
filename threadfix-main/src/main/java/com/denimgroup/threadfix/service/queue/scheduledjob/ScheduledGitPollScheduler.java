package com.denimgroup.threadfix.service.queue.scheduledjob;

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.ScheduledGitPollService;
import com.denimgroup.threadfix.service.queue.GitQueueSender;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledGitPollScheduler extends AbstractScheduledJobScheduler<ScheduledGitPoll>{

    private static final SanitizedLogger log = new SanitizedLogger(ScheduledGitPollScheduler.class);

    @Autowired
    private GitQueueSender gitQueueSender;

    @Autowired
    public ScheduledGitPollScheduler(ScheduledGitPollService scheduledGitPollService){
        super(	scheduledGitPollService,
                ScheduledGitPollJob.class,
                "ScheduledGitPollId_",
                "Scheduled Git Poll",
                "ScheduledGitPolls");
    }

    //Filter out any poll that is not currently enabled
    public boolean addScheduledJob(ScheduledGitPoll scheduledGitPoll) {
        if(scheduledGitPoll.isEnabled())
            return super.addScheduledJob(scheduledGitPoll);
        return false;
    }

    protected Boolean getHasAddedScheduledJob(DefaultConfiguration config){
        return true;
    }

    protected void setHasAddedScheduledJob(DefaultConfiguration config, Boolean bool){}


    protected void setAdditionalJobDataMap(JobDetail job,ScheduledGitPoll scheduledGitPoll){
        job.getJobDataMap().put("applicationId", scheduledGitPoll.getApplication().getId());
        //Since the super assumes the same queue we will replace it
        job.getJobDataMap().put("queueSender",gitQueueSender);
    }

    //TODO override getCronExpression to tweak job scheduling to be more flexible on trigger times

}
