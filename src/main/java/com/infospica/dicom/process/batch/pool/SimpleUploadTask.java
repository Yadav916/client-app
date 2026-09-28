package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.exception.CommandExecutionException;
import com.infospica.dicom.command.pool.SimpleUploadDicomCommandPool;
import com.infospica.dicom.command.pool.UploadDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import com.infospica.dicom.util.LoggerUtility;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleUploadTask implements ExecutableTask<File> {

    private final SimpleUploadDicomCommandPool uploadDicomCommandPool;
    private String[] failedKeywords = new String[]{"Failed", "failed", "Exception", "IOException"};
    public SimpleUploadTask(final SimpleUploadDicomCommandPool uploadDicomCommandPool) {
        this.uploadDicomCommandPool = uploadDicomCommandPool;
    }

    @Override
    public Mono<File> doProcess(File srcFile) throws Exception {
        try {
            String response = uploadDicomCommandPool.execute(srcFile);
            boolean failedTrace = containsWords(response, failedKeywords);
            if(failedTrace) {
                throw new CommandExecutionException("Error processing the file " + srcFile.getAbsolutePath());
            }
            LoggerUtility.log(SimpleUploadTask.class, LoggerUtility.LogLevel.DEBUG, srcFile.getAbsolutePath() + "[Uploaded]");
            Context.updateProcessedImage();
            return Mono.just(srcFile);
        } catch (Exception e) {
            Context.updateProcessedErrorImage();
            LoggerUtility.log(SimpleUploadTask.class,  LoggerUtility.LogLevel.ERROR, "Error during uploading the image: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#ERROR")));
    }

    @Override
    public File doProcess(File srcFile, boolean flag) throws Exception {
        return null;
    }

    @Override
    public void resetCommand() {
        //Do Nothing
    }

    private static boolean containsWords(String inputString, String[] items) {
        boolean found = false;
        for (String item : items) {
            if (inputString.contains(item)) {
                found = true;
                break;
            }
        }
        return found;
    }
}
