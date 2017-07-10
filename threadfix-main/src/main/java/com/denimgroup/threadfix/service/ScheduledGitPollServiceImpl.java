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
