package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.impl.struts.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  See: https://struts.apache.org/docs/restfulactionmapper.html

public class Restful2ActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String parentNamespace) {

        List<StrutsEndpoint> endpoints = list();

        StrutsConfigurationProperties config = project.getConfig();
        String[] actionExtensions = config.get("struts.action.extension", "action,").split(",", -1);

        String idParamName = config.get("struts.mapper.idParameterName", "id");

        for (StrutsPackage strutsPackage : packages) {

            String rootEndpoint = strutsPackage.getNamespace();

            for (StrutsAction action : strutsPackage.getActions()) {

                List<String> possibleMethodNames = list();

                String actionMethod = action.getMethod();
                String httpMethod = "GET";

                //  TODO
                if (true)
                    continue;

                Map<String, ParameterDataType> params = map();
                for (Map.Entry<String,String> entry : action.getParams().entrySet()) {
                    params.put(entry.getKey(), ParameterDataType.getType(entry.getValue()));
                }

                for (String possibleName : possibleMethodNames) {
                    for (String extension : actionExtensions) {
                        String url = PathUtil.combine(rootEndpoint, possibleName);
                        if (extension.length() > 0)
                            url += "." + extension;
                        StrutsEndpoint endpoint = new StrutsEndpoint(action.getActClassLocation(), url, list(httpMethod), params);
                        endpoints.add(endpoint);
                    }
                }
            }
        }

        return endpoints;

    }
}
