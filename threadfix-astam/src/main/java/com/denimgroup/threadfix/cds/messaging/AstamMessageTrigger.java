package com.denimgroup.threadfix.cds.messaging;

import com.secdec.astam.common.messaging.Messaging;

/**
 * Created by amohammed on 7/26/2017.
 */
public interface AstamMessageTrigger {
    void parse(Messaging.AstamMessage message);
}
