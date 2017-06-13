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

/** @author jrios */
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
    @Transactional
    public void onMessage(Message message) {
        try {
            if (message instanceof MapMessage) {
                int applicationId = ((MapMessage)message).getInt("applicationId");
                Application application = applicationService.loadApplication(applicationId);
                if(application != null){
                    gitSourceCodeMonitorService.doCheck(application);
                }else{
                    log.error("Unable to load application : " + applicationId);
                }
            }else{
                log.error("Unsupported message class.");
            }
        } catch (Exception e) {
            log.warn("The JMS message threw an error.");
            e.printStackTrace();
        }
    }
}
