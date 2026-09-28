package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.PreparationTask;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component("_batchCompressImageState")
public class CompressImageState extends AbstractImageState {
    private final PreparationTask preparationTask;
    private BatchDicomFileProcessor processor;

    @Autowired
    public CompressImageState(final PreparationTask preparationTask) {
        super("Batch_Compression_001");
        this.preparationTask = preparationTask;
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
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
        //AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getUploadImageState());
        //AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getSimpleUploadImageState());
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getPushImageState());
        //Context.setPendingImage(0); //reset
        //Context.setCompressedImage(0);
        if(!Context.isCompressionEnabled()) {
            processor.setState(stateAtomicReference.getAndSet(null));
            return;
        }
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Start compressing the images");
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getCompressSourceFolder());

        Context.createMandatoryFolderIfMissing(Context.getCompressDestinationFolder()); //create compressed destination folder if missing
        Context.createMandatoryFolderIfMissing(Context.getLogFolder()); //create log folders if missing

        if(Context.getCompressSourceFolder() != null && Context.getCompressSourceFolder().exists()) {
            //get only the files without "ERROR" suffix.
            Collection<File> files = FileUtils.listFiles(Context.getCompressSourceFolder(),
                    new NotFileFilter(new OrFileFilter(new SuffixFileFilter("#ERROR"), HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
            int fileCount = 0;
            if ((fileCount = files.size()) == 0) {
                processor.setState(processor.getMoveImageState());
                return;
            }
            files.clear(); //deallocate all; no longer used after taking the count.
            Context.setCompressProcessedCount(0);
            String response = preparationTask.compressFile(Context.getCompressSourceFolder(), Context.getCompressDestinationFolder());
            LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, response);
            if (response == null) { //means error; should not continue
                processor.setState(processor.getMoveImageState());
                return;
            }
            List<File> failedFiles = null;
            try {
                List<String> logContents = Arrays.asList(response.split("(\\r\\n|\\r|\\n)"));
                failedFiles = logContents.stream()
                        .filter(s -> s.toLowerCase().startsWith("failed"))
                        .map(fs -> fs.replaceAll("Failed to transcode ", ""))
                        .map(fs1 -> new File(fs1.substring(0, fs1.lastIndexOf(":")).concat("#ERROR")))
                        .collect(Collectors.toList());
                int failedFileCount = failedFiles.size();
                if (failedFileCount > 0) {
                    Scheduler scheduler = Schedulers.boundedElastic();
                    Flux.fromStream(failedFiles.stream()).flatMap(f1 -> {
                                if (!f1.exists()) { //if exists means success
                                    try {
                                        return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                                    } catch (IOException ioe) {
                                        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.ERROR, "Error during moving compress failed image: " + f1.getAbsolutePath());
                                    }
                                }
                                return Mono.empty();
                            }).publishOn(scheduler)
                            .doOnComplete(() -> {
                                LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "Compressed the images");
                            }).blockLast();
                    if (!scheduler.isDisposed()) {
                        scheduler.disposeGracefully();
                    }
                }
                Context.setCompressProcessedCount(fileCount - failedFileCount);
            } catch (Exception e) {
                LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.ERROR, e.getMessage());
            } finally {
                if(failedFiles != null) {
                    failedFiles.clear();
                }
            }
            LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed compression and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }

    @Override
    public void pushImages() {
        LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to push to server");
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
