package com.infospica.dicom.process.batch.pool;

import com.infospica.dicom.command.pool.ExtractDicomCommandPool;
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
public class ExtractTaskPool implements ExecutableTask<String> { //extends ExecutableTaskPool<ExtractTask> implements ExecutableTask<String> {

    private final ExtractDicomCommandPool extractDicomCommandPool;
    private final ExtractTask extractTask;

    @Autowired
    public ExtractTaskPool(final ApplicationContext applicationContext) {
        //super();
        this.extractDicomCommandPool = new ExtractDicomCommandPool(applicationContext);
        this.extractTask = new ExtractTask(this.extractDicomCommandPool);
        //createPool();
    }

    @Override
    public Mono<String> doProcess(File srcFile) throws Exception {
        if(!Context.isUploadServiceStarted()) return Mono.empty();
        try {
            return extractTask.doProcess(srcFile);
        } finally {}
    }

    @Override
    public String doProcess(File srcFile, boolean flag) throws Exception {
        return null;
    }

    @Override
    public void resetCommand() {
        this.extractDicomCommandPool.resetAll();
    }

    /*@Override
    protected ExtractTask create() {
        return new ExtractTask(extractDicomCommandPool);
    }*/
}
