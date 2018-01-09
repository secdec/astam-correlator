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
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsRestPlugin;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import static com.denimgroup.threadfix.CollectionUtils.list;

/*
    CompositeActionMapper:
        Common name: composite
        Full name: org.apache.struts2.dispatcher.mapper.CompositeActionMapper

    DefaultActionMapper:
        Common name: struts
        Full name: org.apache.struts2.dispatcher.mapper.DefaultActionMapper

    PrefixBasedActionMapper:
        Common name: prefix (?) prefixBased (?) (can't find a reference)
        Full name: org.apache.struts2.dispatcher.mapper.PrefixBasedActionMapper

    RestPluginActionMapper:
        Common name: rest
        Full name: org.apache.struts2.rest.RestActionMapper

    Restful2ActionMapper:
        Common name: restful2
        Full name: org.apache.struts2.dispatcher.mapper.Restful2ActionMapper

 */

public class ActionMapperFactory {

    private static final SanitizedLogger log = new SanitizedLogger("ActionMapperFactory");

    StrutsConfigurationProperties config;

    public ActionMapperFactory(StrutsConfigurationProperties config) {
        this.config = config;
    }

    public boolean isMapperConfigured() {
        return config.get("struts.mapper.class") != null;
    }

    public ActionMapper detectMapper(StrutsProject project) {
        ActionMapper result = null;

        if (isMapperConfigured()) {
            String mapperName = config.get("struts.mapper.class");

            result = findMapper(mapperName, project);
        } else {

            if (project.hasPlugin(StrutsRestPlugin.class)) {
                result = new CompositeActionMapper(list(new DefaultActionMapper(), new RestPluginActionMapper()));
            } else {
                log.debug("No mapper configuration detected, using DefaultActionMapper");
                result = new DefaultActionMapper();
            }
        }

        return result;
    }

    ActionMapper findMapper(String mapperName, StrutsProject forProject) {
        ActionMapper result = null;

        if (mapperName == null) {
            return null;
        }

        if (mapperName.equalsIgnoreCase("composite")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.CompositeActionMapper")) {

            result = new CompositeActionMapper(forProject);

        }
        else if (mapperName.equalsIgnoreCase("struts")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.DefaultActionMapper")) {

            result = new DefaultActionMapper();

        }
        else if (mapperName.equalsIgnoreCase("prefix") || mapperName.equalsIgnoreCase("prefixBased")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.PrefixBasedActionMapper")) {

            result = new PrefixBasedActionMapper(forProject);

        }
        else if (mapperName.equalsIgnoreCase("rest")
                || mapperName.equalsIgnoreCase("org.apache.struts2.rest.RestActionMapper")) {

            result = new RestPluginActionMapper();

        }

        return result;
    }
}
