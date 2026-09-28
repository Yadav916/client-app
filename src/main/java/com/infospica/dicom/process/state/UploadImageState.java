package com.infospica.dicom.process.state;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.process.PreparationTask;
import com.infospica.dicom.process.UploadTask;
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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class UploadImageState extends AbstractImageState {

    private final UploadTask uploadTask;
    private DicomUploadProcessor processor;

    public UploadImageState(final UploadTask uploadTask) {
        super("Upload_001");
        this.uploadTask = uploadTask;
    }

    public void setProcessor(DicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already moved the images");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already extracted the images");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already modified the images");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already compressed the images");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Start uploading the images");
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getUploadSourceFolder());
        if(Context.getUploadSourceFolder() != null && Context.getUploadSourceFolder().exists()) {
            //get only the files without "ERROR" suffix or error retry pattern.
            Collection<File> files = FileUtils.listFiles(Context.getUploadSourceFolder(),
                    new NotFileFilter(
                            new OrFileFilter(
                                    new OrFileFilter(
                                            new SuffixFileFilter("#ERROR"),
                                            new WildcardFileFilter("*#ERROR#*")
                                    ),
                                    HiddenFileFilter.HIDDEN
                            )
                    ), TrueFileFilter.INSTANCE);
            if(files.size() == 0) {
                processor.setState(processor.getMoveImageState());
                return;
            }
            Context.setProcessedImage(0);
            File backupFolder = new File(Context.getUploadBackupFolder(), Constants._FOLDER_FORMAT.format(new Date()));
            FileUtility.makeDirectories(backupFolder);
            LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the backup folder: " + backupFolder.getAbsolutePath());

            AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getBackupState());
            Flux.fromStream(files.stream())
                    .parallel()
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(f -> uploadTask.uploadFile(f).flatMap(f1 -> {
                        if(f1.exists()) { //if exists means success
                            try {
                                String relativePath = FileUtility.getRelativePathFrom(f1.getAbsolutePath(), Context.getUploadSourceFolder().getAbsolutePath());
                                File backupFile = new File(backupFolder, relativePath);
                                FileUtils.moveFile(f1, backupFile);
                                return Mono.just(backupFile);
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR, "Error during backup uploaded image: " + f1.getAbsolutePath());
                            }
                        } else {
                            try {
                                return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#UPLOADERROR"));
                            } catch (IOException ioe) {
                                LoggerUtility.log(CompressImageState.class, LoggerUtility.LogLevel.ERROR, "Error during create error image: " + f1.getAbsolutePath());
                            }
                        }
                        return Mono.empty();
                    })).sequential()
                    .publishOn(Schedulers.boundedElastic())
                    .doOnComplete(() -> {
                        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Uploaded the images");
                    }).doOnError(throwable -> {
                        LoggerUtility.log(UploadImageState.class, "Error during uploading: ", throwable);
                }).blockLast();
            LoggerUtility.log(ModifyImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed uploading and moving to next state.");
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
