package com.denimgroup.threadfix.service.queue;


import com.denimgroup.threadfix.data.entities.ExceptionLog;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.denimgroup.threadfix.service.ExceptionLogService;
import com.denimgroup.threadfix.service.repository.GitServiceImpl;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.text.SimpleDateFormat;

@Service
@Transactional
public class GitQueueSenderImpl implements GitQueueSender {

    protected final SanitizedLogger log = new SanitizedLogger(GitQueueSenderImpl.class);
    private String jmsErrorString = "The JMS system encountered an error that prevented the message from being correctly created.";
    private static final SimpleDateFormat format = new SimpleDateFormat("MMM d, y h:mm:ss a");

    @Autowired
    GitServiceImpl gitService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ExceptionLogService exceptionLogService;


    @Override
    public void pollGitRepo(int applicationId) {
        MapMessage  gitPollMap = new ActiveMQMapMessage();
        log.info("Sending message to poll git application" + applicationId + ".");
        try {
            gitPollMap.setString("applicationId", Integer.toString(applicationId));
            jmsTemplate.convertAndSend("gitRequestQueue", gitPollMap);
        } catch (JMSException e) {
            log.error(jmsErrorString);
            addExceptionLog(e);
        }
    }

    private void addExceptionLog(Exception e) {
        ExceptionLog exceptionLog = new ExceptionLog(e);
        exceptionLogService.storeExceptionLog(exceptionLog);
        log.error("Uncaught exception - logging at " + format.format(exceptionLog.getTime().getTime()) + ".");
    }

}
