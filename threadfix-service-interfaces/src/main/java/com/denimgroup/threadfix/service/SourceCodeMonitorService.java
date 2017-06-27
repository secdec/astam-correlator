package com.denimgroup.threadfix.service;

import com.denimgroup.threadfix.data.entities.Application;

/** @author jrios */
public interface SourceCodeMonitorService {

    void doCheck(Application application);

}
