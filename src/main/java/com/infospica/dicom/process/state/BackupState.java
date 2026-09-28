package com.infospica.dicom.process.state;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
public class BackupState extends AbstractImageState {

    private DicomUploadProcessor processor;

    public BackupState() {
        super("Backup_001");
    }
    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Already moved the images");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Already extracted the json");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Already modified the images");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Already compressed the images");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Already uploaded the images");
    }

    @Override
    public void backupImages() {
        /*
            1. clear process folder
            2. move error files back to process folder
            3. clear anonymized folder excluding the error files
        */
        CountDownLatch latch = new CountDownLatch(2);
        new Thread(() -> {
            //clear process folder
            clearProcessedFiles(Context.getProcessSourceFolder());
            renameErrorFiles(Context.getProcessSourceFolder());
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
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "*** Completed backup and moving to next state.");
        processor.setState(processor.getMoveImageState());
    }
}
