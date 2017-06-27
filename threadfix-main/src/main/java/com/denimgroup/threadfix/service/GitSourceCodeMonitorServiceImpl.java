package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeRepoType;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service("gitSourceCodeMonitorServiceImpl")
@Transactional
public class GitSourceCodeMonitorServiceImpl implements SourceCodeMonitorService {

    protected final SanitizedLogger log = new SanitizedLogger(GitSourceCodeMonitorServiceImpl.class);

    @Autowired @Qualifier("gitServiceImpl")
    RepositoryService gitService;

    @Autowired
    SourceCodeStatusService sourceCodeStatusService;


    /***
     * Performs the basic functionality of checking a git repository
     * and determining if it has changed and run appropriate logic thereafter.
     *
     * @param application The application whose code base we are going to check
     */

    @Override
    public void doCheck(Application application) {
        boolean changesDetected = false;
        if (!application.getRepositoryType().equalsIgnoreCase(SourceCodeRepoType.GIT.getRepoType())) {
            log.info(getApplicationNameFormat(application) + " Source code repo type is not Git. Exiting check.");
            return;
        }
        String originHash = gitService.getCurrentRevision(application);
        if(originHash == null || originHash.isEmpty()){
            log.info(getApplicationNameFormat(application) + " Unable to retrieve origin repo hash. Exiting check.");
            return;
        }else{
            log.debug(getApplicationNameFormat(application) + " Origin repo hash found " + originHash);
        }
        SourceCodeStatus sourceCodeStatus = sourceCodeStatusService.getLatest(application);
        if(sourceCodeStatus == null) {
            //if one does not exist populate it for saving
            log.info(getApplicationNameFormat(application) + " Local source code status not found");
            saveNewSourceCodeStatus(application,originHash);
            changesDetected = true;
        }
        else {
            if(sourceCodeStatus.getCommitId().equalsIgnoreCase(originHash)){
                log.info(getApplicationNameFormat(application) + " Latest origin commit matches local source code commitId");
                sourceCodeStatus.setDateTimeChecked(new Date());
                //since we found the same hash, lets update it the row
                sourceCodeStatusService.saveOrUpdate(sourceCodeStatus);
            }else{
                //local commit id has found and does not match the last time we checked
                log.info(getApplicationNameFormat(application) + " Latest origin commit does not match local source code commitId");
                saveNewSourceCodeStatus(application,originHash);
                changesDetected = true;
            }
        }
        //TODO Implement something meaningful here
        if(changesDetected){
            log.info(getApplicationNameFormat(application) + " Git repository changes detected");
        }else
            log.info(getApplicationNameFormat(application) + "Git repository did not detect any changes");
    }


    private void saveNewSourceCodeStatus(Application application,String originHash){
        sourceCodeStatusService.saveNewStatus(application,originHash);
    }


    /** Name formatting for logs*/
    private String getApplicationNameFormat(Application application){
        StringBuilder sb = new StringBuilder();
        if(application != null && application.getName() != null && !application.getName().isEmpty()) {
            sb.append(application.getName());
            if (application.getId() != null)
                sb.append("_" + application.getId());
        }else{sb.append("Invalid Application identifiers.");}
        return sb.toString();
    }
}
