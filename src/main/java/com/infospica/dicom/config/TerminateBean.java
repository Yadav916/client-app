package com.infospica.dicom.config;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;

public class TerminateBean {
    public void terminate() {
        LoggerUtility.log(TerminateBean.class, LoggerUtility.LogLevel.INFO, "Application termination is applied.");
        Context.setTerminate(true);
    }
}
