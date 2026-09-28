package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.pool.ExtractTaskPool;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Component("_batchExtractJSONState")
public class ExtractJSONState extends AbstractImageState {

    private BatchDicomFileProcessor processor;
    private final ExtractTaskPool extractTaskPool;
    protected ExecutorService extractExecutorService = Executors.newFixedThreadPool(5);

    @Autowired
    public ExtractJSONState(final ExtractTaskPool extractTaskPool) {
        super("Batch_ExtractJSON_001");
        this.extractTaskPool = extractTaskPool;
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Already moved the files");
    }

    @Override
    public void extractJsonFromImages() {
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getModifyImageState());
        //Context.setPendingImage(0); //reset
        //Context.setJsonExtractImage(0);
        if(!Context.isJsonExtractionEnabled()) { //means extraction not enabled; move to next state
            processor.setState(stateAtomicReference.getAndSet(null));
            return;
        }
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Extract json from dicom");
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getProcessSourceFolder());
        if(Context.getProcessSourceFolder() != null && Context.getProcessSourceFolder().exists()) {
            //get only the files without "ERROR" suffix.
            Collection<File> files = FileUtils.listFiles(Context.getProcessSourceFolder(),
                    new NotFileFilter(
                            new OrFileFilter(new SuffixFileFilter("#ERROR"), HiddenFileFilter.HIDDEN)
                    ), TrueFileFilter.INSTANCE);
            if(files.size() == 0) {
                processor.setState(processor.getMoveImageState());
                return;
            }
            //Context.setPendingImage(fileCount);
            Context.setJsonProcessedCount(0);
            CountDownLatch latch = new CountDownLatch(1);
            Scheduler singleScheduler = Schedulers.single();
            Scheduler executorScheduler = Schedulers.fromExecutorService(extractExecutorService);
            File jsonStoreFile = new File(Context.getExtractJsonFolder(), "json_" + Constants._FOLDER_FORMAT.format(new Date()) + ".txt");
            Flux.fromStream(files.stream()).parallel(5)
                .runOn(executorScheduler)
                .flatMap(f -> {
                    if(!f.exists()) return Mono.empty();
                    try {
                        return extractTaskPool.doProcess(f)
                                .flatMap(r -> {
                                    if(!r.equalsIgnoreCase("#ERROR")) { //if exists means success
                                        try {
                                            FileUtils.writeStringToFile(jsonStoreFile, (r + "\r\n"), true);
                                            Context.updateJsonExtractImage();
                                        } catch (IOException e) {}
                                        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Persist the extracted json");
                                    }
                                    return Mono.empty();
                                });
                    } catch (Exception e) { e.printStackTrace(); }
                    return Mono.empty();
                })
                .sequential().publishOn(singleScheduler)
                .doOnComplete(() -> {
                    LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Extracted json from the images");
                    latch.countDown();
                }).doOnError(throwable -> {
                    LoggerUtility.log(ExtractJSONState.class, "Error during extract json: ", throwable);
                    stateAtomicReference.set(processor.getMoveImageState());
                    latch.countDown();
                }).subscribe();
            try {
                latch.await();
                processor.setState(stateAtomicReference.getAndSet(null));
            } catch (InterruptedException e) {
                LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.WARN, "Extraction process is interrupted.");
            } finally {
                if(files != null) {
                    files.clear(); //deallocate all
                }
            }
            if(!singleScheduler.isDisposed()) singleScheduler.disposeGracefully();
            if(!executorScheduler.isDisposed()) executorScheduler.disposeGracefully();

            LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "*** Completed extraction and moving to next state.");
        }
    }

    @Override
    public void modifyImages() {
        if(!Context.isAnonymizeEnabled()) return;
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to modify");
    }

    @Override
    public void compressImages() {
        if(!Context.isCompressionEnabled()) return;
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to compress");
    }

    @Override
    public void pushImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to push to server");
    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }

    @PreDestroy
    public void shutdownGracefully() {
        if(!extractExecutorService.isShutdown()) {
            extractExecutorService.shutdown();
        }
    }
}
