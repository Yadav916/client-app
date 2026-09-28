package com.infospica.dicom.process.state;

import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.process.PreparationTask;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ModifyImageState extends AbstractImageState {

    private final PreparationTask preparationTask;
    private DicomUploadProcessor processor;

    @Autowired
    public ModifyImageState(final PreparationTask preparationTask) {
        super("Modify_001");
        this.preparationTask = preparationTask;
    }

    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Already moved the files");
    }

    @Override
    public void extractJsonFromImages() {
        if(!Context.isJsonExtractionEnabled()) return;
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Already extracted the images");
    }

    @Override
    public void modifyImages() {
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getCompressImageState()); //define next state
        Context.setModifiedImage(0);
        if(!Context.isAnonymizeEnabled()) { //means modification not enabled; move to next state
            processor.setState(stateAtomicReference.getAndSet(null));
            return;
        }
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Start modify the images");
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getProcessSourceFolder());
        if(Context.getProcessSourceFolder() != null && Context.getProcessSourceFolder().exists()) {
            Collection<File> files = FileUtils.listFiles(Context.getProcessSourceFolder(),new NotFileFilter(
                    new OrFileFilter(new SuffixFileFilter("#ERROR"), HiddenFileFilter.HIDDEN)
            ), TrueFileFilter.INSTANCE);
            if(files.size() == 0) {
                processor.setState(processor.getMoveImageState()); //restart
                return;
            }

            Flux.fromStream(files.stream())
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(f -> preparationTask.updateNameAndAddress(f)
                            .flatMap(f1 -> {
                                if(!f1.exists()) { //if exists means success
                                    try {
                                        return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                                    } catch (IOException ioe) {
                                        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.ERROR, "Error during processing modify image: " + f1.getAbsolutePath());
                                    }
                                }
                                return Mono.empty();
                            })).sequential()
                    .doOnComplete(() -> {
                        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Modified the images");
                    }).doOnError(throwable -> {
                        LoggerUtility.log(ModifyImageState.class, "Error during processing modify images: ", throwable);
                        stateAtomicReference.set(processor.getBackupState());
                    }).blockLast();
            LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed modification and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }

    @Override
    public void compressImages() {
        if(!Context.isCompressionEnabled()) return;
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to compress");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
