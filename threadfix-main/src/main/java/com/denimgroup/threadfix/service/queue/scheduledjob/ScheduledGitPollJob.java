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
