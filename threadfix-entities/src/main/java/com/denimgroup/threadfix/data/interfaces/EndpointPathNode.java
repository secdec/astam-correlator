package com.denimgroup.threadfix.data.interfaces;

import javax.annotation.Nonnull;

public interface EndpointPathNode {

    boolean matches(@Nonnull String pathPart);
    boolean matches(@Nonnull EndpointPathNode node);

}
