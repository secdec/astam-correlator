package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;
import com.denimgroup.threadfix.service.GenericObjectService;

/** @author jrios */
public interface SourceCodeStatusService extends GenericObjectService<SourceCodeStatus> {

 SourceCodeStatus getLatest(Application application);

 void saveNewStatus(Application application,String commitId);

}
