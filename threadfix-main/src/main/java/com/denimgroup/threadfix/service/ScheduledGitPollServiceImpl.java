package com.denimgroup.threadfix.service;


import com.denimgroup.threadfix.data.dao.ScheduledGitPollDao;
import com.denimgroup.threadfix.data.dao.ScheduledJobDao;
import com.denimgroup.threadfix.data.entities.ScheduledGitPoll;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
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
}
