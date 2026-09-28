package com.infospica.dicom.process.state;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.state.batch.BackupState;
import com.infospica.dicom.process.state.batch.MoveImageState;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractImageState implements DicomProcessState {

    private String id;

    protected AbstractImageState(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    protected int moveFilesToProcessFolder(MoveTask moveTask, File sourceFolder, File destinationFolder) {
        return moveFilesToProcessFolder(moveTask, sourceFolder, destinationFolder, null, false);
    }

    protected int moveFilesToProcessFolder(MoveTask moveTask, File sourceFolder, File destinationFolder, Consumer<File> fileConsumer) {
        return moveFilesToProcessFolder(moveTask, sourceFolder, destinationFolder, fileConsumer, false);
    }

    protected int moveFilesToProcessFolder(MoveTask moveTask, File sourceFolder, File destinationFolder, Consumer<File> fileConsumer, Boolean randomize) {
        int[] status = new int[] { 0 };
        String maskFolderString = sourceFolder.getAbsolutePath();
        //Collection<File> files = FileUtils.listFiles(sourceFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Collection<File> files = FileUtils.listFiles(sourceFolder,
                new NotFileFilter(new OrFileFilter(new SuffixFileFilter(".part"), HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
        if(files.isEmpty()) return status[0];

        LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO,
                "The source folder: " + sourceFolder.getAbsolutePath() + ", destination folder: " + destinationFolder.getAbsolutePath());
        AtomicInteger fileCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler singleScheduler = Schedulers.single();

        Disposable disposable = Flux.fromIterable(files)
            .flatMap(ff -> {
                fileCount.incrementAndGet();
                return moveTask.moveFileForProcessing(ff, maskFolderString, destinationFolder, randomize);
            })
            .flatMap(ff -> {
                if(fileConsumer != null) {
                    fileConsumer.accept(ff);
                }
                return Mono.just(ff);
            })
            .publishOn(singleScheduler)
            .doOnComplete(() -> {
                LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "Moved the files to processing folder.");
                latch.countDown();
            }).doOnError(throwable -> {
                status[0] = -99;
                LoggerUtility.log(AbstractImageState.class, "Error during moving files for processing: ", throwable);
                latch.countDown();
            }).subscribe();
        try {
            latch.await(); //wait parent
            status[0] = fileCount.get();
        } catch (InterruptedException e) {
            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.WARN, "Move image process is interrupted.");
            status[0] = -777;
        } finally {
            if(!singleScheduler.isDisposed()) singleScheduler.disposeGracefully();
            if(!disposable.isDisposed()) disposable.dispose();
        }
        return status[0];
    }
    protected void renameErrorFiles(File folder) {
        if (!folder.exists()) return;
        Collection<File> files = FileUtils.listFiles(folder, new WildcardFileFilter("*#ERROR*"), TrueFileFilter.INSTANCE);
        if (!files.isEmpty()) {
            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, 
                "Processing " + files.size() + " error file(s) for retry in folder: " + folder.getAbsolutePath());
            
            Integer retryLimit = Context.getStoreSCPConfigurationProperties().getRetryLimit();
            if (retryLimit == null || retryLimit < 1) {
                retryLimit = 5; // Default retry limit
            }
            
            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, 
                "Retry limit configured as: " + retryLimit);
            
            File backupErrorFolder = new File(Context.getUploadBackupFolder(), "error");
            FileUtility.makeDirectories(backupErrorFolder);
            
            final Integer finalRetryLimit = retryLimit;
            Scheduler scheduler = Schedulers.boundedElastic();
            Flux.fromStream(files.stream())
                    .flatMap(f -> {
                        try {
                            int currentRetryCount = FileUtility.getErrorRetryCount(f);
                            
                            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, 
                                "Checking file: " + f.getName() + " | Current retry count: " + currentRetryCount + " | Limit: " + finalRetryLimit);
                            
                            if (currentRetryCount >= finalRetryLimit) {
                                // Exceeded retry limit - move to backup error folder
                                return Mono.fromCallable(() -> {
                                    LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.WARN, 
                                        "File exceeded retry limit (count=" + currentRetryCount + ", limit=" + finalRetryLimit + "): " + f.getName());
                                    
                                    File cleanFile = FileUtility.removeErrorRetry(f);
                                    String relativePath = FileUtility.getRelativePathFrom(
                                        cleanFile.getAbsolutePath(), 
                                        folder.getAbsolutePath()
                                    );
                                    File backupFile = new File(backupErrorFolder, relativePath);
                                    
                                    LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.WARN, 
                                        "Moving from: " + f.getAbsolutePath());
                                    LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.WARN, 
                                        "Moving to: " + backupFile.getAbsolutePath());
                                    
                                    FileUtility.makeDirectories(backupFile.getParentFile());
                                    FileUtils.moveFile(f, backupFile);
                                    
                                    LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.WARN, 
                                        "Successfully moved file to error backup after " + currentRetryCount + " retries: " + f.getName());
                                    return f;
                                }).subscribeOn(scheduler);
                            } else {
                                // Increment retry count and rename for next attempt
                                return Mono.fromCallable(() -> {
                                    File newFile = FileUtility.incrementErrorRetry(f);
                                    FileUtils.moveFile(f, newFile);
                                    LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, 
                                        "Incremented retry count to " + (currentRetryCount + 1) + " for: " + f.getName());
                                    return newFile;
                                }).subscribeOn(scheduler);
                            }
                        } catch (Exception e) {
                            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.ERROR, 
                                "Error processing retry for file: " + f.getName() + " - " + e.getMessage());
                            return Mono.empty();
                        }
                    })
                    .doOnError(throwable -> {
                        LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.ERROR, 
                            "Error during rename error files: " + throwable.getMessage());
                    })
                    .doOnComplete(() -> {
                        LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "Processed all error files with retry limit: " + finalRetryLimit);
                    })
                    .blockLast();
            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "*** Processed error files from the folder " + folder.getAbsolutePath());
            if(!scheduler.isDisposed()) scheduler.disposeGracefully();
        }
    }

    protected void clearProcessedFiles(File folder) {
        if (!folder.exists()) return;

        Collection<File> files = FileUtils.listFiles(folder,
                new NotFileFilter(new OrFileFilter(new SuffixFileFilter("#ERROR"), new SuffixFileFilter(".part"))),
                TrueFileFilter.INSTANCE);
        if (!files.isEmpty()) {
            Scheduler scheduler = Schedulers.boundedElastic();
            Flux.fromStream(files.stream())
                    .flatMap(f -> this.deleteFile(f, scheduler))
                    .doOnError(throwable -> {
                    })
                    .doOnComplete(() -> {
                        LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "Deleted all files");
                    })
                    .blockLast();
            if(!scheduler.isDisposed()) {
                scheduler.disposeGracefully();
            }
            LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "*** Cleared files from the folder " + folder.getAbsolutePath());
        }
    }

    protected void clearEmptyFolders(File folder, File srcDependent) {
        if(!folder.exists()) return;

        List<File> folders = (List<File>)FileUtils.listFilesAndDirs(folder, new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
        if(folders.isEmpty()) return;

        Collections.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int io1 = StringUtils.countMatches(o1.getAbsolutePath(), File.separatorChar);
                int io2 = StringUtils.countMatches(o2.getAbsolutePath(), File.separatorChar);
                return Integer.compare(io2, io1);
            }
        });

        folders.removeIf(f -> f.getAbsolutePath().equalsIgnoreCase(folder.getAbsolutePath()));
        Scheduler scheduler = Schedulers.single();
        Flux.fromStream(folders.stream())
            .flatMapSequential(f -> this.deleteFolder(f, scheduler))
            .flatMapSequential(f -> {
                if(srcDependent != null && srcDependent.exists()) {
                    File depPath = new File(srcDependent, FileUtility.getRelativePathFrom(f.getAbsolutePath(), folder.getAbsolutePath()));
                    try {
                        this.deleteFolder(depPath, true);
                    } catch (IOException e) {
                    }
                    return Mono.just(depPath);
                }
                return Mono.just(f);
            })
            .doOnError(throwable -> {})
            .doOnComplete(() -> {
                LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "Deleted all empty folders");
            })
            .blockLast();
        if(!scheduler.isDisposed()) scheduler.disposeGracefully();
        LoggerUtility.log(AbstractImageState.class, LoggerUtility.LogLevel.INFO, "*** Deleted all empty folders inside folder " + folder.getAbsolutePath());
    }
    protected Mono<File> deleteFile(File file, Scheduler scheduler) {
        return Mono.fromCallable(() -> {
            FileUtils.delete(file);
            return file;
        }).subscribeOn(scheduler);
    }

    protected Mono<File> deleteFolder(File file, Scheduler scheduler) {
        return Mono.fromCallable(() -> deleteFolder(file, true)).subscribeOn(scheduler);
    }

    protected File deleteFolder(File file, boolean flag) throws IOException {
        if(FileUtils.isEmptyDirectory(file)) {
            FileUtils.deleteDirectory(file);
        }
        return file;
    }
}
