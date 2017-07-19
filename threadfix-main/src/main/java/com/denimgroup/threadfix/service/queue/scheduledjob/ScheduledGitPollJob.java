package com.denimgroup.threadfix.service.queue.scheduledjob;

import com.denimgroup.threadfix.service.queue.GitQueueSender;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Date;

public class ScheduledGitPollJob implements Job {

    private static final SanitizedLogger log = new SanitizedLogger(ScheduledGitPollJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().toString();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        log.info("ScheduledGitPollJob " + jobName + "for app " +
                dataMap.getInt("applicationId") +
                " executing at " + new Date() + ". Sending request to queue.");

        GitQueueSender gitQueueSender = (GitQueueSender) dataMap.get("queueSender");
        if(gitQueueSender == null){
            log.info("ScheduledGitPollJob " + jobName + " failed to find GitQueueSender at " + new Date());
            return;
        }
        Integer applicationId;
        try{
            applicationId = dataMap.getInt("applicationId");
        }catch(ClassCastException e){
            log.info("ScheduledGitPollJob " + jobName + " failed to cast at " + new Date() + " :" + e.toString());
            return;
        }
        log.info("ScheduledGitPollJob " + jobName + " executing at " + new Date() + ".");
        gitQueueSender.pollGitRepo(applicationId);
    }
}
