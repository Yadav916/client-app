package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.pool.UploadDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.batch.pool.UploadTask;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import com.infospica.dicom.process.schedule.task.ExecutableTaskPool;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
public class UploadTaskPool implements ExecutableTask<File> { // extends ExecutableTaskPool<UploadTask> implements ExecutableTask<File> {

    private final UploadDicomCommandPool uploadDicomCommandPool;
    private final UploadTask uploadTask;

    @Autowired
    public UploadTaskPool(final ApplicationContext applicationContext) {
        //super();
        this.uploadDicomCommandPool = new UploadDicomCommandPool(applicationContext);
        this.uploadTask = new UploadTask(uploadDicomCommandPool);
        //createPool();
    }


    @Override
    public Mono<File> doProcess(File srcFile) throws Exception {
        if(!Context.isUploadServiceStarted()) return Mono.empty();
        /*UploadTask uploadTask = acquire();
        LoggerUtility.log(UploadTaskPool.class, LoggerUtility.LogLevel.DEBUG, "UploadTask instance: " + uploadTask.toString());
        try {
            return uploadTask.doProcess(srcFile);
        } finally {
            recycle(uploadTask);
        }*/
        try {
            return uploadTask.doProcess(srcFile);
        } finally {
        }
    }

    @Override
    public File doProcess(File srcFile, boolean flag) throws Exception {
        /*UploadTask uploadTask = acquire();
        try {
            return uploadTask.doProcess(srcFile, flag);
        } finally {
            recycle(uploadTask);
        }*/
        try {
            return uploadTask.doProcess(srcFile, flag);
        } finally {
        }
    }

    @Override
    public void resetCommand() {
        this.uploadDicomCommandPool.resetAll();
    }

    /*@Override
    protected UploadTask create() {
        return new UploadTask(uploadDicomCommandPool);
    }*/

}
