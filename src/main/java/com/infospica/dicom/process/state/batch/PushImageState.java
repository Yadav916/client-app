package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component("_batchPushImageState")
public class PushImageState extends AbstractImageState {

    private final MoveTask moveTask;
    private BatchDicomFileProcessor processor;

    public PushImageState(final MoveTask moveTask) {
        super("Batch_Push_001");
        this.moveTask = moveTask;
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are moved");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are extracted");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are modified");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are compressed");
    }

    @Override
    public void pushImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "Start pushing the images");
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getUploadSourceFolder());
        Context.createMandatoryFolderIfMissing(Context.getUploadProcessFolder()); //create upload process folder if missing
        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getMoveImageState());
        if(Context.getUploadSourceFolder() != null && Context.getUploadSourceFolder().exists()) {
            //move all files to process directory
            moveFilesToProcessFolder(moveTask, Context.getUploadSourceFolder(), Context.getUploadProcessFolder(), null, true);
        }
        //once moved; clean the process folders
        CountDownLatch latch = new CountDownLatch(3);
        new Thread(() -> {
            //clear process folder
            clearProcessedFiles(Context.getProcessSourceFolder());
            renameErrorFiles(Context.getProcessSourceFolder());
            renameErrorFiles(Context.getUploadProcessFolder());
            clearEmptyFolders(Context.getProcessSourceFolder(), Context.getMachineDestinationFolder());
            latch.countDown();
        }).start();
        new Thread(() -> {
            if (Context.isAnonymizeEnabled()) {
                // rename error modified files back to original name
                clearProcessedFiles(Context.getAnonymizedDestinationFolder());
                renameErrorFiles(Context.getAnonymizedDestinationFolder());
                clearEmptyFolders(Context.getAnonymizedDestinationFolder(), null);
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            if (Context.isCompressionEnabled()) {
                clearProcessedFiles(Context.getCompressDestinationFolder());
                renameErrorFiles(Context.getCompressDestinationFolder());
                clearEmptyFolders(Context.getCompressDestinationFolder(), null);
            }
            latch.countDown();
        }).start();
        try {
            latch.await();
        } catch(InterruptedException ie) {

        }
        processor.setState(stateAtomicReference.getAndSet(null));
    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are uploaded");
    }

    @Override
    public void backupImages() {
        LoggerUtility.log(PushImageState.class, LoggerUtility.LogLevel.INFO, "No images are backup");
    }
}
