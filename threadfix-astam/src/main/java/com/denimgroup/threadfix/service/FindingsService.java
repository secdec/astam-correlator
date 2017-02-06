package com.denimgroup.threadfix.service;

import com.secdec.astam.common.data.models.Findings;

/**
 * Created by jsemtner on 2/3/2017.
 */
public interface FindingsService {
    Findings.RawFindings getFindings(int applicationId);
}
