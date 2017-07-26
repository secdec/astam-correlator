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

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.ScheduledGitPollService;
import com.denimgroup.threadfix.service.queue.GitQueueSender;
import com.microsoft.tfs.core.clients.build.flags.ScheduleType;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ScheduledGitPollScheduler extends AbstractScheduledJobScheduler<ScheduledGitPoll>{

    private static final SanitizedLogger log = new SanitizedLogger(ScheduledGitPollScheduler.class);

    @Autowired
    private GitQueueSender gitQueueSender;

    @Autowired
    public ScheduledGitPollScheduler(@Lazy ScheduledGitPollService scheduledGitPollService){
        super(	scheduledGitPollService,
                ScheduledGitPollJob.class,
                "ScheduledGitPollId_",
                "Scheduled Git Poll",
                "ScheduledGitPolls");
    }

    public boolean addScheduledJob(ScheduledGitPoll scheduledGitPoll) {
        if(scheduledGitPoll.isEnabled()) {
            super.addScheduledJob(scheduledGitPoll);
        }
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

}
