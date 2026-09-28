package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.pool.SimpleUploadDicomCommandPool;
import com.infospica.dicom.command.pool.UploadDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import com.infospica.dicom.process.schedule.task.ExecutableTaskPool;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
public class SimpleUploadTaskPool implements ExecutableTask<File> {

    private final SimpleUploadDicomCommandPool uploadDicomCommandPool;
    private final SimpleUploadTask simpleUploadTask;
    @Autowired
    public SimpleUploadTaskPool(final ApplicationContext applicationContext) {
        this.uploadDicomCommandPool = new SimpleUploadDicomCommandPool(applicationContext);
        this.simpleUploadTask = new SimpleUploadTask(this.uploadDicomCommandPool);
    }


    @Override
    public Mono<File> doProcess(File srcFile) throws Exception {
        if(!Context.isUploadServiceStarted()) return Mono.empty();
        try {
            return simpleUploadTask.doProcess(srcFile);
        } finally {

        }
    }

    @Override
    public File doProcess(File srcFile, boolean flag) throws Exception {
        try {
            return simpleUploadTask.doProcess(srcFile, flag);
        } finally {
        }
    }

    @Override
    public void resetCommand() {
        this.uploadDicomCommandPool.resetAll();
    }
}
