////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.dao.ScheduledGitPollDao;
import com.denimgroup.threadfix.data.dao.ScheduledJobDao;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ScheduledFrequencyType;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;
import com.denimgroup.threadfix.data.entities.ScheduledPeriodType;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.queue.scheduledjob.ScheduledGitPollScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class ScheduledGitPollServiceImpl extends ScheduledJobServiceImpl<ScheduledGitPoll> implements ScheduledGitPollService{

    private final SanitizedLogger log = new SanitizedLogger(ScheduledDefectTrackerUpdateServiceImpl.class);

    @Autowired
    ScheduledGitPollDao scheduledGitPollDao;

    @Override
    protected ScheduledJobDao<ScheduledGitPoll> getScheduledJobDao() {
        return scheduledGitPollDao;
    }

    @Lazy
    @Autowired
    ScheduledGitPollScheduler gitPollScheduler;


    @Override
    public int save(ScheduledGitPoll poll){
        int id = super.save(poll);
        if(id > 0){
            updatePoll(poll);
        }
        return id;
    }

    @Override
    public ScheduledGitPoll loadByApplication(Application application) {
        return scheduledGitPollDao.loadByApplication(application);
    }

    @Override
    public void updatePoll(ScheduledGitPoll poll) {
        gitPollScheduler.removeScheduledJob(poll);
        if(poll.isEnabled()){
            gitPollScheduler.addScheduledJob(poll);
        }
    }

    @Override
    public ScheduledGitPoll loadByApplicationOrDefault(Application application){
        ScheduledGitPoll poll = scheduledGitPollDao.loadByApplication(application);
        if(poll == null)
            poll = getDefaultNew();
        return poll;
    }


    private ScheduledGitPoll getDefaultNew(){
        ScheduledGitPoll poll = new ScheduledGitPoll();
        poll.setFrequency(ScheduledFrequencyType.DAILY.toString());
        poll.setPeriod(ScheduledPeriodType.AM.toString());
        poll.setHour(6);
        return poll;
    }

}
