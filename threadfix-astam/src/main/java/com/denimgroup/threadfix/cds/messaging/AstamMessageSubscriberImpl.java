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
package com.denimgroup.threadfix.cds.messaging;

import com.denimgroup.threadfix.logging.SanitizedLogger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.secdec.astam.common.messaging.Messaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.jms.*;


/**
 * Created by amohammed on 7/17/2017.
 */

@Component
public class AstamMessageSubscriberImpl implements AstamMessageSubscriber, Runnable{

    private static final SanitizedLogger LOGGER = new SanitizedLogger(AstamMessageSubscriberImpl.class);

    private Connection connection;
    private Session session;
    private String topicString;
    private String brokerUrl;

    @Autowired private AstamMessageTrigger messageTrigger;


    public AstamMessageSubscriberImpl(){
    }

    @Override
    public void setup(@Nonnull Connection connection, @Nonnull String topicString){
        this.connection = connection;
        this.topicString = topicString;
    }

    public void receiveMessage() throws JMSException, com.google.protobuf.InvalidProtocolBufferException {
        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicString);
        String subName = Thread.currentThread().getName();
        TopicSubscriber topicSubscriber = session.createDurableSubscriber(topic, subName);
        Message message = topicSubscriber.receive();
        if(message instanceof BytesMessage){
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) ((BytesMessage) message).getBodyLength()];
            bytesMessage.readBytes(bytes);
            Messaging.AstamMessage astamMessage = Messaging.AstamMessage.parseFrom(bytes);
            LOGGER.info("ASTAM message received: " + astamMessage.toString());
            messageTrigger.parse(astamMessage);
            message.acknowledge();
        }
    }

    @Override
    public void run() {
        try {
            receiveMessage();
        } catch (InvalidClientIDException ice){
            LOGGER.error("Error caused by invalid Client ID: ", ice);
        }catch (JMSException jmse) {
            LOGGER.error("JMS Exception error: ", jmse);
        } catch (InvalidProtocolBufferException ipbe){
            LOGGER.error("Error parsing astam protobuf message", ipbe);
        }
    }
}
