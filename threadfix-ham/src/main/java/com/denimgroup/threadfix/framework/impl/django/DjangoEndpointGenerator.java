package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * Created by csotomayor on 4/27/2017.
 */
public class DjangoEndpointGenerator implements EndpointGenerator{
    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return null;
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return null;
    }
}
