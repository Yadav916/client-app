package com.infospica.dicom.process.schedule.task;

import com.infospica.dicom.command.pool.CompressDicomCommandPool;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
public class CompressTaskPool implements ExecutableTask<File> { //extends ExecutableTaskPool<CompressTask> implements ExecutableTask<File> {

    private final CompressDicomCommandPool compressDicomCommandPool;
    private final CompressTask compressTask;

    @Autowired
    public CompressTaskPool(final ApplicationContext applicationContext) {
        //super();
        this.compressDicomCommandPool = new CompressDicomCommandPool(applicationContext);
        this.compressTask = new CompressTask(this.compressDicomCommandPool);
        //createPool();
    }


    @Override
    public Mono<File> doProcess(File srcFile) throws Exception {
        /*CompressTask compressTask = acquire();
        LoggerUtility.log(CompressTaskPool.class, LoggerUtility.LogLevel.DEBUG, "CompressTask instance: " + compressTask.toString());
        try {
            return compressTask.doProcess(srcFile);
        } finally {
            recycle(compressTask);
        }*/
        try {
            return compressTask.doProcess(srcFile);
        } finally {

        }
    }

    @Override
    public File doProcess(File srcFile, boolean flag) throws Exception {
        /*CompressTask compressTask = acquire();
        try {
            return compressTask.doProcess(srcFile, flag);
        } finally {
            recycle(compressTask);
        }*/
        try {
            return compressTask.doProcess(srcFile, flag);
        } finally{

        }
    }

    @Override
    public void resetCommand() {
        this.compressDicomCommandPool.resetAll();
    }

    /*@Override
    protected CompressTask create() {
        return new CompressTask(compressDicomCommandPool);
    }*/

}
