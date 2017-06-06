package com.denimgroup.threadfix.service.queue;

import com.denimgroup.threadfix.service.SourceCodeMonitorService;
import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.*;
import com.denimgroup.threadfix.service.repository.GitServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.jms.*;

@Component
public class GitQueueListener implements MessageListener{

    protected final SanitizedLogger log = new SanitizedLogger(GitQueueListener.class);

    @Autowired @Qualifier(value = "gitSourceCodeMonitorServiceImpl")
    private SourceCodeMonitorService gitSourceCodeMonitorService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private GitServiceImpl gitService;

    @Override
    @Transactional(readOnly=false)
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage) {
                MapMessage map = (MapMessage) message;
                int applicationId = map.getInt("applicationId");
                Application application = applicationService.loadApplication(applicationId);
                if(application != null){
                    gitSourceCodeMonitorService.doCheck(application);
                }else{
                    log.error("Unable to load application : " + applicationId);
                }
            }
        } catch (Exception e) {
            log.warn("The JMS message threw an error.");
            e.printStackTrace();
        }
    }
}
