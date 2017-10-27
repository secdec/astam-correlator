package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;

import java.util.Collection;
import java.util.List;

public interface ActionMapper {

    List<StrutsEndpoint> generateEndpoints(StrutsProject project, String namespace);

    Collection<StrutsKnownPlugins> getRequiredPlugins();
}
