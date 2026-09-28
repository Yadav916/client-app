package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.process.batch.pool.SimpleUploadTaskPool;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.process.state.CompressImageState;
import com.infospica.dicom.util.EchoServer;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Component("_batchSimpleUploadState")
public class SimpleUploadImageState extends AbstractImageState {

    private final MoveTask moveTask;
    private final SimpleUploadTaskPool uploadTaskPool;
    private BatchDicomUploadProcessor processor;
    protected ExecutorService extractExecutorService = Executors.newFixedThreadPool(5);

    public SimpleUploadImageState(final MoveTask moveTask, final SimpleUploadTaskPool uploadTaskPool) {
        super("Simple_Batch_Upload_001");
        this.moveTask = moveTask;
        this.uploadTaskPool = uploadTaskPool;
    }

    public void setProcessor(BatchDicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Already moved the images");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Already extracted the images");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Already modified the images");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Already compressed the images");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Start uploading the images");
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getUploadSourceFolder());
        if(Context.getUploadSourceFolder() != null && Context.getUploadSourceFolder().exists()) {
            Set<File> fileSet = new HashSet<>();

            Collection<File> existingFiles = FileUtils.listFiles(Context.getUploadProcessFolder(),
                    new NotFileFilter(new OrFileFilter(HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
            fileSet.addAll(existingFiles);

            //move all files to process directory
            moveFilesToProcessFolder(moveTask, Context.getUploadSourceFolder(), Context.getUploadProcessFolder(), fileSet::add);

            //check whether server is available or not
            if(!EchoServer.isRemoteServerListening()) {
                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR, "Unable to upload this time, server not responding.");
                processor.setState(processor.getUploadSuspendState());
                fileSet.clear();
                return;
            }
            Queue<File> fileQueue = new LinkedList<>();
            fileQueue.addAll(fileSet);
            fileQueue.removeIf(f -> f.getName().contains("#ERROR")); //skip the error files (including #ERROR#1, #ERROR#2, etc.)

            Context.setProcessedImage(0);
            Context.setProcessedErrorImage(0);


            File backupFolder = new File(Context.getUploadBackupFolder(), Constants._FOLDER_FORMAT.format(new Date()));
            FileUtility.makeDirectories(backupFolder);
            LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the backup folder: " + backupFolder.getAbsolutePath());

            AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getBackupState());
            CountDownLatch latch = new CountDownLatch(1);
            Scheduler singleScheduler = Schedulers.single();
            Scheduler executorScheduler = Schedulers.fromExecutorService(extractExecutorService);

            Flux.fromStream(fileQueue.stream())
                    .parallel(5)
                    .runOn(executorScheduler)
                    .flatMap(f -> {
                        if(!f.exists()) return Mono.empty();
                        try {
                            return uploadTaskPool.doProcess(f)
                                    .flatMap(f1 -> {
                                        if(f1.exists()) { //if exists means success
                                            try {
                                                String relativePath = FileUtility.getRelativePathFrom(f1.getAbsolutePath(), Context.getUploadProcessFolder().getAbsolutePath());
                                                File backupFile = new File(backupFolder, relativePath);
                                                FileUtils.moveFile(f1, backupFile);
                                                return Mono.just(backupFile);
                                            } catch (IOException ioe) {
                                                ioe.printStackTrace();
                                                LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.ERROR, "Error during backup uploaded image: " + f1.getAbsolutePath());
                                            }
                                        } else {
                                            try {
                                                return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                                            } catch (IOException ioe) {
                                                LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.ERROR, "Error during create error image: " + f1.getAbsolutePath());
                                            }
                                        }
                                        return null;
                                    });
                        } catch(Exception ee) {

                        }
                        return Mono.empty();
                    }).sequential()
                    .publishOn(singleScheduler)
                    .doOnComplete(() -> {
                        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "Uploaded the images");
                        latch.countDown();
                    }).doOnError(throwable -> {
                        LoggerUtility.log(SimpleUploadImageState.class, "Error during uploading: ", throwable);
                        latch.countDown();
                    }).subscribe();
            try {
                latch.await();
            } catch (InterruptedException e) {
                LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.WARN, "Upload process is interrupted.");
            }
            if(!singleScheduler.isDisposed()) singleScheduler.disposeGracefully();
            if(!executorScheduler.isDisposed()) executorScheduler.disposeGracefully();

            LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed uploading and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(SimpleUploadImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
