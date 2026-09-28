package com.infospica.dicom.process.schedule.task;

import com.infospica.dicom.command.pool.CompressDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import reactor.core.publisher.Mono;

import java.io.File;

public class CompressTask implements ExecutableTask<File> {
    private final CompressDicomCommandPool compressDicomCommandPool;

    public CompressTask(final CompressDicomCommandPool compressDicomCommandPool) {
        this.compressDicomCommandPool = compressDicomCommandPool;
    }

    @Override
    public Mono<File> doProcess(File srcFile) {
        try {
            //compressFile = ((ExecutableCommand)applicationContext.getBean("compressDicomCommand")).execute(srcFile.getAbsolutePath());
            String compressFile = compressDicomCommandPool.execute(srcFile);
            LoggerUtility.log(CompressTask.class, LoggerUtility.LogLevel.DEBUG, compressFile + "[Compressed]");
            Context.updateCompressedImage();
            return Mono.just(new File(compressFile));
        } catch (Exception e) {
            LoggerUtility.log(CompressTask.class,  LoggerUtility.LogLevel.ERROR, "Error during compressing the image: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#ERROR")));
    }

    @Override
    public File doProcess(File srcFile, boolean flag) {
        return null;
    }

    @Override
    public void resetCommand() {
        this.compressDicomCommandPool.resetAll();
    }
}
