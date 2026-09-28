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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CompressImageState extends AbstractImageState {
    private final PreparationTask preparationTask;
    private DicomUploadProcessor processor;

    public CompressImageState(final PreparationTask preparationTask) {
        super("Compression_001");
        this.preparationTask = preparationTask;
    }

    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Already moved the images");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Already extracted the images");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Already modified the images");
    }

    @Override
    public void compressImages() {
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getUploadImageState());
        if(!Context.isCompressionEnabled()) {
            processor.setState(stateAtomicReference.getAndSet(null));
            return;
        }
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Start compressing the images");
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getCompressSourceFolder());
        if(Context.getCompressSourceFolder() != null && Context.getCompressSourceFolder().exists()) {
            //get only the files without "ERROR" suffix.
            Collection<File> files = FileUtils.listFiles(Context.getCompressSourceFolder(),
                    new NotFileFilter(new OrFileFilter(new SuffixFileFilter("#ERROR"), HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
            if(files.size() == 0) {
                processor.setState(processor.getMoveImageState());
                return;
            }
            Context.setCompressedImage(0);
            Flux.fromStream(files.stream())
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(f -> preparationTask.compressFile(f).flatMap(f1 -> {
                        if(!f1.exists()) { //if exists means success
                            try {
                                return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                            } catch (IOException ioe) {
                                LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.ERROR, "Error during compressing image: " + f1.getAbsolutePath());
                            }
                        }
                        return Mono.empty();
                    })).sequential()
                    .doOnComplete(() -> {
                        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Compressed the images");
                    }).doOnError(throwable -> {
                        LoggerUtility.log(CompressImageState.class, "Error during compression processing: ", throwable);
                        stateAtomicReference.set(processor.getBackupState());
                    }).blockLast();

            LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed compression and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
