package com.infospica.dicom.process.batch;

import com.infospica.dicom.command.pool.CompressDicomCommandPool;
import com.infospica.dicom.command.pool.ModifyDicomCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component("_batchPreparationType")
public class PreparationTask {

    @Autowired
    @Qualifier("batchModifyDicomCommand")
    ExecutableCommand batchModifyDicomCommand;

    @Autowired
    @Qualifier("batchCompressFileCommand")
    ExecutableCommand batchCompressFileCommand;

    public String updateNameAndAddress(File srcFolder, File destFolder) {
        try {
            String response = this.batchModifyDicomCommand.execute(srcFolder.getAbsolutePath(), destFolder.getAbsolutePath());
            return response;
        } catch (Exception e) {
            LoggerUtility.log(PreparationTask.class,  LoggerUtility.LogLevel.ERROR, "Error during update: Cause" + e.getMessage());
        }
        return null;
    }

    public String compressFile(File srcFolder, File destFolder) {
        try {
            String response = batchCompressFileCommand.execute(srcFolder.getAbsolutePath(), destFolder.getAbsolutePath());
            return response;
        } catch (Exception e) {
            LoggerUtility.log(PreparationTask.class,  LoggerUtility.LogLevel.ERROR, "Error during compressing the image: Cause" + e.getMessage());
        }
        return null;
    }

}
