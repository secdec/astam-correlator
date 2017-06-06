package com.denimgroup.threadfix.data.dao;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.SourceCodeStatus;

/**
 * Created by jrios on 6/5/2017.
 */
public interface SourceCodeStatusDao {

    SourceCodeStatus getLatest(Application application);
}
