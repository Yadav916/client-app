package com.infospica.dicom.process.state;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MoveImageState extends AbstractImageState {

    private final MoveTask moveTask;
    private DicomUploadProcessor processor;

    @Autowired
    public MoveImageState(final MoveTask moveTask) {
        super("Move_001");
        this.moveTask = moveTask;
    }

    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Start moving the files");
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getMachineDestinationFolder());
        if(Context.getMachineDestinationFolder() != null && Context.getMachineDestinationFolder().exists()) {
            Collection<File> files = FileUtils.listFiles(Context.getMachineDestinationFolder(),
                    new NotFileFilter(new OrFileFilter(HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
            Context.setPendingImage(0); //always reset
            if (files.size() == 0) {
                processor.setState(processor.getMoveImageState());
                return;
            }
            AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getExtractJson());
            int fileCount = moveFilesToProcessFolder(moveTask, Context.getMachineDestinationFolder(), Context.getProcessSourceFolder());
            if(fileCount < 0) {
                stateAtomicReference.set(processor.getMoveImageState());
            }
            Context.setPendingImage(fileCount);
            processor.setState(stateAtomicReference.getAndSet(null));

            /*CountDownLatch latch = new CountDownLatch(1);
            Flux.fromStream(files.stream())
                    .parallel(5)
                    .flatMap(moveTask::moveFileForProcessing).sequential()
                    .publishOn(Schedulers.single())
                    .doOnComplete(() -> {
                        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Moved the files to processing folder.");
                        latch.countDown();
                    }).doOnError(throwable -> {
                        LoggerUtility.log(MoveImageState.class, "Error during moving files for processing: ", throwable);
                        stateAtomicReference.set(processor.getMoveImageState());
                        latch.countDown();
                    }).subscribe();

            try {
                latch.await(); //wait parent
                processor.setState(stateAtomicReference.getAndSet(null));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
        }
    }

    @Override
    public void extractJsonFromImages() {
        if(!Context.isJsonExtractionEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are extracted");

    }

    @Override
    public void modifyImages() {
        if(!Context.isAnonymizeEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to modify");
    }

    @Override
    public void compressImages() {
        if(!Context.isCompressionEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to compress");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
