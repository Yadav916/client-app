package com.infospica.dicom.process.schedule;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.ExtractTask;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.PreparationTask;
import com.infospica.dicom.process.UploadTask;
import com.infospica.dicom.process.schedule.task.CompressTaskPool;
import com.infospica.dicom.process.state.BackupState;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultScheduleProcessManager extends AbstractScheduleProcessManager {
    private final Object lockObject = new Object();

    private CompressTaskPool compressTaskPool;

    public DefaultScheduleProcessManager(final MoveTask moveTask, final PreparationTask preparationTask, final ExtractTask extractTask, final UploadTask uploadTask) {
        super(moveTask, preparationTask, extractTask, uploadTask);
    }

    public DefaultScheduleProcessManager(final CompressTaskPool compressTaskPool, final MoveTask moveTask, final PreparationTask preparationTask, final ExtractTask extractTask, final UploadTask uploadTask) {
        super(moveTask, preparationTask, extractTask, uploadTask);
        this.compressTaskPool = compressTaskPool;
    }

    @Override
    public void moveImages() {
        synchronized (lockObject) {
            while(this.moveFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }
        LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Start moving the files");
        LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getMachineDestinationFolder());
        Flux<File> pFileFlux = this.moveFileFlux.publishOn(moveImageScheduler);
        pFileFlux.doOnNext(f -> {
            if(Context.isJsonExtractionEnabled()) {
                try {
                    extractQueue.put(f);
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "ExtractQueue [PUT]: " + e.getMessage());
                }
            }
        })
        .doOnNext(f -> {
            if(Context.isAnonymizeEnabled()) {
                try {
                    modifyQueue.put(f);
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "ModifyQueue [PUT]: " + e.getMessage());
                }
            }
        })
        .doOnNext(f -> {
            if(!Context.isAnonymizeEnabled() && Context.isCompressionEnabled()) {
                try {
                    compressQueue.put(f);
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "CompressQueue [PUT]: " + e.getMessage());
                }
            }
        })
        .subscribe();
    }

    @Override
    public void extractJsonFromImages() {
        synchronized (lockObject) {
            while(this.extractFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }

        this.extractFileFlux
                .flatMap(extractTask::toJson)
                .publishOn(extractJsonScheduler)
                .doOnComplete(() -> {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Extracted the images");
                }).doOnError(throwable -> {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, "Error during extracting: ", throwable);
                }).blockLast();
    }

    @Override
    public void modifyImages() {
        synchronized (lockObject) {
            while(this.modifyFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }

        this.modifyFileFlux
            .flatMap(f -> preparationTask.updateNameAndAddress(f)
                .flatMap(f1 -> {
                    if (!f1.exists()) { //if exists means success
                        try {
                            return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                        } catch (IOException ioe) {
                            LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Error during processing modify image: " + f1.getAbsolutePath());
                        }
                    }
                    try {
                        this.compressQueue.put(f1);
                    } catch (InterruptedException e) {
                        LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "CompressQueue [PUT] after update: " + e.getMessage());
                    }
                    return Mono.empty();
            })).publishOn(modifyScheduler)
            .doOnComplete(() -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Modified the images");
            }).doOnError(throwable -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, "Error during modification: ", throwable);
            }).blockLast();
    }

    @Override
    public void compressImages() {
        synchronized (lockObject) {
            while(this.compressFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }
        this.compressFileFlux
            //.flatMap(f -> preparationTask.compressFile(f).flatMap(f1 -> {
            .flatMap(f -> {
                try {
                    return this.compressTaskPool.doProcess(f).flatMap(f1 -> {
                        if (!f1.exists()) { //if exists means success
                            try {
                                return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#ERROR"));
                            } catch (IOException ioe) {
                                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Error during compressing image: " + f1.getAbsolutePath());
                            }
                        }
                        try {
                            this.uploadQueue.put(f1);
                        } catch (InterruptedException e) {
                            LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "UploadQueue [PUT] after compress: " + e.getMessage());
                        }
                        return Mono.empty();
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).publishOn(Schedulers.fromExecutor(compressExecutorService)).subscribe();
            /*.doOnComplete(() -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Compressed the images");
            }).doOnError(throwable -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, "Error during compression: ", throwable);
            }).blockLast();*/
    }

    public void uploadImages() {
        synchronized (lockObject) {
            while(this.uploadFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }
        this.uploadFileFlux
            .flatMap(f -> uploadTask.uploadFile(f).flatMap(f1 -> {
                if(f1.exists()) { //if exists means success
                    try {
                        this.backupQueue.put(f1);
                    } catch (InterruptedException e) {
                        LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "BackupQueue [PUT] after upload: " + e.getMessage());
                    }
                    return Mono.just(f1);
                } else {
                    try {
                        return Mono.just(FileUtility.moveAsErrorFile(f1, null, "#UPLOADERROR"));
                    } catch (IOException ioe) {
                        LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Error during create error image: " + f1.getAbsolutePath());
                    }
                }
                return Mono.empty();
            })).publishOn(Schedulers.fromExecutor(uploadExecutorService)).subscribe();
            /*.doOnComplete(() -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.INFO, "Uploaded the images");
            }).doOnError(throwable -> {
                LoggerUtility.log(DefaultScheduleProcessManager.class, "Error during uploading: ", throwable);
            }).blockLast();*/
    }

    @Override
    public void backupImages() {
        synchronized (lockObject) {
            while(this.backupFileFlux == null) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.WARN, e.getMessage());
                }
            }
        }
        AtomicInteger bkFileCount = new AtomicInteger(0);
        this.backupFileFlux
            .flatMap(f -> {
                String relativePath = FileUtility.getRelativePathFrom(f.getAbsolutePath(), Context.getUploadSourceFolder().getAbsolutePath());
                return createUploadedFileBackup(f, relativePath);
            }).publishOn(Schedulers.fromExecutor(backupExecutorService))
            .flatMap(r -> deleteFile(Context.getProcessSourceFolder(), r))
            .flatMap(r -> deleteFile(Context.getAnonymizedDestinationFolder(), r))
            .flatMap(r -> deleteFile(Context.getCompressDestinationFolder(), r))
            //.flatMap(r -> clearEmptyFolders(Context.getProcessSourceFolder(), r))
            .publishOn(Schedulers.fromExecutor(backupExecutorService)).subscribe(new BaseSubscriber<String>() {
                @Override
                protected void hookOnNext(String value) {
                    if(bkFileCount.incrementAndGet() > 1000) {
                        bkFileCount.set(0);
                        Context.setCurrentUploadBackupFolder();
                    }
                }
            });
    }

    private Mono<String> createUploadedFileBackup(File file, String relativePath) {
        return Mono.fromCallable(() -> {
            File backupFile = new File(Context.getCurrentUploadBackupFolder(), relativePath);
            if (backupFile.exists()) {
                try {
                    FileUtils.moveFile(backupFile, new File(backupFile.getParentFile(), backupFile.getName().concat("_(" + System.currentTimeMillis() + ")")));
                } catch (IOException e) {
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Error during backup of existing image: " + backupFile.getAbsolutePath());
                }
            }
            try {
                FileUtils.moveFile(file, backupFile);
                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "Created backup of : " + backupFile.getAbsolutePath());
            } catch (IOException e) {
                LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Error during backup of image: " + file.getAbsolutePath());
            }
            return relativePath;
        }).subscribeOn(Schedulers.boundedElastic());
    }



    @Override
    public void prepare() {
        synchronized (lockObject) {
            initFileFlux();
            lockObject.notifyAll();
        }
    }

}
