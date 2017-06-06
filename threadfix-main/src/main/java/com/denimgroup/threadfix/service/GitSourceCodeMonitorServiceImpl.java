package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeRepoType;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service("gitSourceCodeMonitorServiceImpl")
@Transactional(readOnly = false)
public class GitSourceCodeMonitorServiceImpl implements SourceCodeMonitorService {

    protected final SanitizedLogger log = new SanitizedLogger(GitSourceCodeMonitorServiceImpl.class);

    @Autowired @Qualifier("gitServiceImpl")
    RepositoryService gitService;

    @Autowired
    SourceCodeStatusService sourceCodeStatusService;

    @Override
    public void doCheck(Application application) {
        //verify app type
        boolean startHam = false;
        if (!application.getRepositoryType().equalsIgnoreCase(SourceCodeRepoType.GIT.getRepoType())) {
            log.info("Source code repo type is not Git. Exiting check.");
            return;
        }
        //retrieve the current hash given app type
        String originHash = gitService.getCurrentRevision(application);
        if(originHash.isEmpty()){
            log.info("Unable to retrieve origin repo hash. Exiting check.");
            return;
        }
        //pull the last status saved in TF
        SourceCodeStatus sourceCodeStatus = sourceCodeStatusService.getLatest(application);
        if(sourceCodeStatus == null) { //if one does not exist populate it for saving
            log.info("No local source code status found");
            sourceCodeStatus = new SourceCodeStatus();
            sourceCodeStatus.setApplication(application);
            sourceCodeStatus.setRepoType(SourceCodeRepoType.getType(application.getRepositoryType()));
            sourceCodeStatus.setDateTimeChecked(new Date());
            sourceCodeStatus.setCommitId(originHash);
            sourceCodeStatusService.saveOrUpdate(sourceCodeStatus);
            startHam = true;
        }
        else {
            if(sourceCodeStatus.getCommitId().equalsIgnoreCase(originHash)){
                log.info("Latest origin commit matches local source code");
                sourceCodeStatus.setDateTimeChecked(new Date());
                //since we found the same hash, lets update it the row
                sourceCodeStatusService.saveOrUpdate(sourceCodeStatus);
            }else{
                //local commit id has found and does not match the last time we checked
                log.info("Latest origin commit does not match local source code");
                SourceCodeStatus newSourceCodeStatus = new SourceCodeStatus();
                newSourceCodeStatus.setApplication(application);
                newSourceCodeStatus.setRepoType(SourceCodeRepoType.getType(application.getRepositoryType()));
                newSourceCodeStatus.setDateTimeChecked(new Date());
                newSourceCodeStatus.setCommitId(originHash);
                sourceCodeStatusService.saveOrUpdate(newSourceCodeStatus);
                startHam = true;
            }
        }
        if(startHam){
            //TODO: Start Ham or queue up
        }
    }

}
