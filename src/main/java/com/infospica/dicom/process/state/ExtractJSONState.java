package com.infospica.dicom.process.state;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.process.ExtractTask;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ExtractJSONState extends AbstractImageState {

    private DicomUploadProcessor processor;
    private final ExtractTask extractTask;

    @Autowired
    public ExtractJSONState(final ExtractTask extractTask) {
        super("ExtractJSON_001");
        this.extractTask = extractTask;
    }

    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Already moved the files");
    }

    @Override
    public void extractJsonFromImages() {
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getModifyImageState());
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
            Context.setJsonProcessedCount(0);
            Flux.fromStream(files.stream())
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(f -> extractTask.toJson(f)
                            .flatMap(r -> {
                                if(!r.equalsIgnoreCase("#ERROR")) { //if exists means success
                                    LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Persist the extracted json");

                                }
                                return Mono.empty();
                            })).sequential()
                    .doOnComplete(() -> {
                        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "Extracted json from the images");
                    }).doOnError(throwable -> {
                        LoggerUtility.log(ExtractJSONState.class, "Error during extract json: ", throwable);
                        stateAtomicReference.set(processor.getMoveImageState());
                    }).blockLast();

            LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "*** Completed extraction and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
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

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(ExtractJSONState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
