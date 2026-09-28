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

@Component("_batchModifyImageState")
public class ModifyImageState extends AbstractImageState {

    private final PreparationTask preparationTask;
    private BatchDicomFileProcessor processor;

    @Autowired
    public ModifyImageState(final PreparationTask preparationTask) {
        super("Batch_Modify_001");
        this.preparationTask = preparationTask;
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
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
        //Context.setPendingImage(0); //reset
        //Context.setModifiedImage(0);
        if(!Context.isAnonymizeEnabled()) { //means modification not enabled; move to next state
            processor.setState(stateAtomicReference.getAndSet(null));
            return;
        }
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Start modify the images");
        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getProcessSourceFolder());

        Context.createMandatoryFolderIfMissing(Context.getAnonymizedDestinationFolder()); //create mandatory folders if missing
        Context.createMandatoryFolderIfMissing(Context.getLogFolder()); //create log folder if missing

        if(Context.getProcessSourceFolder() != null && Context.getProcessSourceFolder().exists()) {
            Collection<File> files = FileUtils.listFiles(Context.getProcessSourceFolder(), new NotFileFilter(
                    new OrFileFilter(new SuffixFileFilter("#ERROR"), HiddenFileFilter.HIDDEN)
            ), TrueFileFilter.INSTANCE);
            int fileCount = 0;
            if((fileCount = files.size()) == 0) {
                processor.setState(processor.getMoveImageState()); //restart
                return;
            }
            files.clear(); //deallocate all; after taking the count it is no longer required.

            Context.setModifiedProcessedCount(0);
            String response = preparationTask.updateNameAndAddress(Context.getProcessSourceFolder(), Context.getAnonymizedDestinationFolder());
            LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, response);
            if(response == null) { //means error; should not continue
                processor.setState(stateAtomicReference.getAndSet(null));
                return;
            }
            try {
                List<String> logContents = Arrays.asList(response.split("(\\r\\n|\\r|\\n)"));
                List<File> failedFiles = logContents.stream()
                        .filter(s -> s.toLowerCase().startsWith("failed"))
                        .map(fs -> fs.replaceAll("Failed to de-identify ", ""))
                        .map(fs1 -> new File(fs1.substring(0, fs1.lastIndexOf(":")).trim().concat("#ERROR")))
                        .collect(Collectors.toList());
                int failedFileCount = failedFiles.size();
                if (failedFileCount > 0) {
                    Scheduler scheduler = Schedulers.boundedElastic();
                    Flux.fromStream(failedFiles.stream()).flatMap(f1 -> {
                                if (!f1.exists()) { //if exists means success
                                    try {
                                        return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                                    } catch (IOException ioe) {
                                        LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.ERROR, "Error during moving modify failed image: " + f1.getAbsolutePath());
                                    }
                                }
                                return Mono.empty();
                            }).publishOn(scheduler)
                            .doOnComplete(() -> {
                                LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "Modified the images");
                            }).blockLast();
                    if (!scheduler.isDisposed()) {
                        scheduler.disposeGracefully();
                    }
                }
                Context.setModifiedProcessedCount(fileCount - failedFileCount);
            } catch (Exception e) {
                LoggerUtility.log(ModifyImageState.class, "Error during processing anonymize. Cause: " + e.getMessage(), e);
            }
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
