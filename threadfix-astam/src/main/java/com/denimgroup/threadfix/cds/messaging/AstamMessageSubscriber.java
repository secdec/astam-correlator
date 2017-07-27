package com.denimgroup.threadfix.cds.messaging;

import javax.annotation.Nonnull;
import javax.jms.Connection;

/**
 * Created by amohammed on 7/26/2017.
 */
public interface AstamMessageSubscriber {
    void setup(@Nonnull Connection connection, @Nonnull String topicString);
}
