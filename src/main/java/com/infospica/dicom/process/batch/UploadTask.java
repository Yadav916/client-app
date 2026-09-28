package com.infospica.dicom.process.batch;

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component("_batchUploadType")
public class UploadTask {

    @Autowired
    @Qualifier("batchUploadFileCommand")
    ExecutableCommand batchUploadFileCommand;

    public String uploadFile(File srcFolder) {
        try {
            return this.batchUploadFileCommand.execute(srcFolder.getAbsolutePath());
        } catch (Exception e) {
            LoggerUtility.log(UploadTask.class,  LoggerUtility.LogLevel.ERROR, "Error during uploading the image. Cause: " + e.getMessage());
        }
        return null;
    }
}
