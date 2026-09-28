package com.infospica.dicom.service;

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SCUService {

    @Autowired
    @Qualifier("scuEchoCommand")
    private ExecutableCommand echoCommand;

    public boolean isServerReachable() {
        try {
            LoggerUtility.log(SCUService.class, LoggerUtility.LogLevel.INFO, "Ping the server");
            String response = echoCommand.execute((Object[]) null);
            LoggerUtility.log(SCUService.class, LoggerUtility.LogLevel.INFO, "Response: " + response);
            return response.equalsIgnoreCase("SUCCESS");
        } catch (Exception e) { }
        return false;
    }
}
