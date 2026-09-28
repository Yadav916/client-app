package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.pool.ExtractDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import com.infospica.dicom.util.LoggerUtility;
import reactor.core.publisher.Mono;

import java.io.File;

public class ExtractTask implements ExecutableTask<String>  {

    private final ExtractDicomCommandPool extractDicomCommandPool;

    public ExtractTask(final ExtractDicomCommandPool extractDicomCommandPool) {
        this.extractDicomCommandPool = extractDicomCommandPool;
    }

    @Override
    public Mono<String> doProcess(File srcFile) throws Exception {
        try {
            //String result = ((ExecutableCommand)applicationContext.getBean("extractDicomCommand")).execute(srcFile.getAbsolutePath());
            String result = extractDicomCommandPool.execute(srcFile);
            //LoggerUtility.log(ExtractTask.class, LoggerUtility.LogLevel.DEBUG, (result.length() > 100 ? result.substring(0, 100) + "..." : result));
            //Context.updateJsonExtractImage();
            return Mono.just(result);
        } catch (Exception e) {
            LoggerUtility.log(ExtractTask.class,  LoggerUtility.LogLevel.ERROR, "Error during extract json from the image: Cause" + e.getMessage());
        }
        return Mono.just("#ERROR");
    }

    @Override
    public String doProcess(File srcFile, boolean flag) throws Exception {
        return null;
    }

    @Override
    public void resetCommand() {
        //Do Nothing
    }
}
