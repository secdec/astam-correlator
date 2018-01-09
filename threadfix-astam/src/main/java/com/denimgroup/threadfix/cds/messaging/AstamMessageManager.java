package com.denimgroup.threadfix.cds.messaging;

import com.secdec.astam.common.messaging.Messaging;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by amohammed on 7/26/2017.
 */
public interface AstamMessageManager {
    void notify(@Nonnull Messaging.AstamMessage.DataMessage.DataEntity dataEntity,
                @Nonnull Messaging.AstamMessage.DataMessage.DataAction dataAction,
                @Nonnull Messaging.AstamMessage.DataMessage.DataSetType dataSetType,
                @Nonnull List<String> entityIds);

    void subscribe(@Nonnull Messaging.AstamMessage.DataMessage.DataEntity dataEntity,
                   @Nonnull Messaging.AstamMessage.DataMessage.DataAction dataAction,
                   @Nonnull Messaging.AstamMessage.DataMessage.DataSetType dataSetType);
}
