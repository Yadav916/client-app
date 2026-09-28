package com.infospica.dicom.service;

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SCPService implements Monitorable<Boolean> {

    @Autowired
    @Qualifier("scpStartCommand")
    private ExecutableCommand startCommand;

    @Autowired
    @Qualifier("scpStopCommand")
    private ExecutableCommand stopCommand;

    @Autowired
    @Qualifier("scpEchoCommand")
    private ExecutableCommand scpEchoCommand;

    private DicomUploadProcessor processor;


    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    public void start() {
        try {
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Start listening for dicom files");
            String response = startCommand.execute((Object[]) null);
            Context.updateSCPEchoResponse(response);
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Stop listening for dicom files");
            String response = stopCommand.execute((Object[]) null);
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean isRunning() {
        try {
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Ping the SCP server");
            String response = scpEchoCommand.execute((Object[]) null);
            LoggerUtility.log(SCPService.class, LoggerUtility.LogLevel.INFO, "Response: " + response);
            return response.equalsIgnoreCase("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
