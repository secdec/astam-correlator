package com.denimgroup.threadfix.framework.impl.struts.conventions;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;

import java.util.Collection;

public class RestConvention extends Convention {

    String idParameterName;
    String indexMethodName;
    String getMethodName;
    String postMethodName;
    String putMethodName;
    String deleteMethodName;
    String editMethodName;
    String newMethodName;

    final String editMethodViewName = "edit";
    final String newMethodViewName = "editNew";

    public RestConvention(StrutsConfigurationProperties config, Collection<StrutsPackage> packages) {
        super(config, packages);

        idParameterName     = config.get("struts.mapper.idParameterName",   "id");
        indexMethodName     = config.get("struts.mapper.indexMethodName",   "index");
        getMethodName       = config.get("struts.mapper.getMethodName",     "show");
        postMethodName      = config.get("struts.mapper.postMethodName",    "create");
        putMethodName       = config.get("struts.mapper.putMethodName",     "update");
        deleteMethodName    = config.get("struts.mapper.deleteMethodName",  "destroy");
        editMethodName      = config.get("struts.mapper.editMethodName",    "edit");
        newMethodName       = config.get("struts.mapper.newMethodName",     "editNew");
    }
}
