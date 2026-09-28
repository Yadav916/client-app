package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.pool.UploadDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import com.infospica.dicom.util.LoggerUtility;
import reactor.core.publisher.Mono;

import java.io.File;

public class UploadTask implements ExecutableTask<File> {

    private final UploadDicomCommandPool uploadDicomCommandPool;

    public UploadTask(final UploadDicomCommandPool uploadDicomCommandPool) {
        this.uploadDicomCommandPool = uploadDicomCommandPool;
    }

    @Override
    public Mono<File> doProcess(File srcFile) throws Exception {
        try {
            String uploadedFile = uploadDicomCommandPool.execute(srcFile);
            LoggerUtility.log(UploadTask.class, LoggerUtility.LogLevel.DEBUG, srcFile.getAbsolutePath() + "[Uploaded]");
            Context.updateProcessedImage();
            return Mono.just(srcFile);
        } catch (Exception e) {
            LoggerUtility.log(UploadTask.class,  LoggerUtility.LogLevel.ERROR, "Error during uploading the image: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#ERROR")));
    }

    @Override
    public File doProcess(File srcFile, boolean flag) throws Exception {
        return null;
    }

    @Override
    public void resetCommand() {

    }
}
