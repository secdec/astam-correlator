package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  See: https://struts.apache.org/docs/restfulactionmapper.html

public class RestPluginActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String namespace) {
        return null;
    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        return list(StrutsKnownPlugins.STRUTS2_REST);
    }
}
