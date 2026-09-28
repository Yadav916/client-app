/**
 * Licensed to the Cirakas Consulting Pvt Ltd under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.cirakas.com/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infospica.dicom.process.schedule;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.ExtractTask;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.PreparationTask;
import com.infospica.dicom.process.UploadTask;
import com.infospica.dicom.process.state.BackupState;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *   @author arun.vs
 */
public abstract class AbstractScheduleProcessManager implements ScheduleProcessManager {

    //define queues
    protected final BlockingQueue<File> extractQueue;
    protected final BlockingQueue<File> modifyQueue;
    protected final BlockingQueue<File> compressQueue;
    protected final BlockingQueue<File> uploadQueue;
    protected final BlockingQueue<File> backupQueue;

    protected Flux<File> moveFileFlux = null;
    protected Flux<File> extractFileFlux = null;
    protected Flux<File> modifyFileFlux = null;
    protected Flux<File> compressFileFlux = null;
    protected Flux<File> uploadFileFlux = null;
    protected Flux<File> backupFileFlux = null;

    protected final MoveTask moveTask;
    protected final PreparationTask preparationTask;
    protected final ExtractTask extractTask;
    protected final UploadTask uploadTask;

    //pools
    protected ExecutorService compressExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    protected ExecutorService uploadExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    protected ExecutorService backupExecutorService = Executors.newCachedThreadPool();
    protected Scheduler moveImageScheduler = Schedulers.newParallel("Move-Thread", 2);
    protected Scheduler extractJsonScheduler = Schedulers.boundedElastic();
    protected Scheduler modifyScheduler = Schedulers.newParallel("Modify-Thread", 5);

    protected AbstractScheduleProcessManager(final MoveTask moveTask, final PreparationTask preparationTask, final ExtractTask extractTask, final UploadTask uploadTask) {
        this.extractQueue = new ArrayBlockingQueue<>(50);
        this.modifyQueue = new ArrayBlockingQueue<>(50);
        this.compressQueue = new LinkedBlockingQueue<>(50);
        this.uploadQueue = new LinkedBlockingQueue<>(50);
        //this.backupQueue = new ArrayBlockingQueue<>(10);
        this.backupQueue = new LinkedBlockingQueue<>(50);

        this.moveTask = moveTask;
        this.preparationTask = preparationTask;
        this.extractTask = extractTask;
        this.uploadTask = uploadTask;
    }

    private Flux<File> createMoveFileFlux() {
        if(Context.getMachineDestinationFolder() != null && Context.getMachineDestinationFolder().exists()) {
            return Flux.create((FluxSink<File> fluxSink) -> {
                while (!Context.isTerminated() && Context.isUploadServiceStarted()) {
                    Collection<File> files = FileUtils.listFiles(Context.getMachineDestinationFolder(),
                            TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                    files.stream()
                            .map(f -> moveTask.moveFileForProcessing(f, Context.getMachineDestinationFolder().getAbsolutePath(), Context.getProcessSourceFolder(), true))
                            .filter(f1 -> !Objects.isNull(f1))
                            .forEach(f -> {
                                Context.setPendingImage(1);
                                fluxSink.next(f);
                            });
                    try {
                        /*if (!FileUtils.isEmptyDirectory(Context.getMachineDestinationFolder())) {
                            clearEmptyFolders(Context.getMachineDestinationFolder());
                        }*/
                        Thread.sleep(5000);
                    //} catch (IOException ioe) {

                    } catch (InterruptedException e) {
                        LoggerUtility.log(AbstractScheduleProcessManager.class, LoggerUtility.LogLevel.ERROR, "Sleep error: move process");
                    }
                }
                fluxSink.complete();
            });
        }
        return null;
    }

    private Flux<File> createExtractFileFlux(FileFluxCompleteHandler fileFluxCompleteHandler) {
        return Flux.create(fluxSink -> {
            try {
                do {
                    File file = extractQueue.take();
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "ExtractQueue: " + extractQueue.size() + ", " + file.getAbsolutePath());
                    fluxSink.next(file);
                    if(Context.isTerminated() && Context.isUploadServiceStarted()) break;
                } while(!extractQueue.isEmpty());
                fluxSink.complete();
                if(fileFluxCompleteHandler != null) {
                    fileFluxCompleteHandler.onComplete();
                }
            } catch (InterruptedException e) {
            }
        });
    }

    private Flux<File> createModifyFileFlux(FileFluxCompleteHandler fileFluxCompleteHandler) {
        return Flux.create(fluxSink -> {
            try {
                do {
                    File file = modifyQueue.take();
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "ModifyQueue: " + modifyQueue.size() + ", " + file.getAbsolutePath());
                    fluxSink.next(file);
                    if(Context.isTerminated() && Context.isUploadServiceStarted()) break;
                } while(!extractQueue.isEmpty());
                fluxSink.complete();
                if(fileFluxCompleteHandler != null) {
                    fileFluxCompleteHandler.onComplete();
                }
            } catch (InterruptedException e) {

            }
        });
    }

    private Flux<File> createCompressFileFlux(FileFluxCompleteHandler fileFluxCompleteHandler) {
        return Flux.create((FluxSink<File> fluxSink) -> {
            try {
                do {
                    File file = compressQueue.take();
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "CompressQueue: " + compressQueue.size() + ", " + file.getAbsolutePath());
                    fluxSink.next(file);
                } while(!Context.isTerminated() && Context.isUploadServiceStarted());
                fluxSink.complete();
            } catch (InterruptedException e) {

            }
        });
    }

    private Flux<File> createUploadFileFlux(FileFluxCompleteHandler fileFluxCompleteHandler) {
        return Flux.create((FluxSink<File> fluxSink) -> {
            try {
                do {
                    File file = uploadQueue.take();
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "UploadQueue: " + uploadQueue.size() + ", " + file.getAbsolutePath());
                    fluxSink.next(file);
                } while(!Context.isTerminated() && Context.isUploadServiceStarted());
                fluxSink.complete();
            } catch (InterruptedException e) {

            }
        });
    }

    private Flux<File> createBackupFileFlux(FileFluxCompleteHandler fileFluxCompleteHandler) {
        return Flux.create((FluxSink<File> fluxSink) -> {
            try {
                do {
                    File file = backupQueue.take();
                    LoggerUtility.log(DefaultScheduleProcessManager.class, LoggerUtility.LogLevel.DEBUG, "BackupQueue: " + backupQueue.size() + ", " + file.getAbsolutePath());
                    fluxSink.next(file);
                } while(!Context.isTerminated() && Context.isUploadServiceStarted());
                fluxSink.complete();
            } catch (InterruptedException e) {

            }
        });
    }

    protected void initFileFlux() {
        if(this.moveFileFlux == null) {
            this.moveFileFlux = createMoveFileFlux();
        }
        if(this.extractFileFlux == null && Context.isJsonExtractionEnabled()) {
            this.extractFileFlux = createExtractFileFlux(extractCompleteHandler);
        }
        if(this.modifyFileFlux == null && Context.isAnonymizeEnabled()) {
            this.modifyFileFlux = createModifyFileFlux(modifyCompleteHandler);
        }
        if(this.compressFileFlux == null && Context.isCompressionEnabled()) {
            this.compressFileFlux = createCompressFileFlux(compressCompleteHandler);
        }
        if(this.uploadFileFlux == null) {
            this.uploadFileFlux = createUploadFileFlux(uploadCompleteHandler);
        }
        if(this.backupFileFlux == null) {
            this.backupFileFlux = createBackupFileFlux(backupCompleteHandler);
        }
    }


    final FileFluxCompleteHandler extractCompleteHandler = () -> this.extractFileFlux = null;
    final FileFluxCompleteHandler modifyCompleteHandler = () -> this.modifyFileFlux = null;
    final FileFluxCompleteHandler compressCompleteHandler = () -> this.compressFileFlux = null;
    final FileFluxCompleteHandler uploadCompleteHandler = () -> this.uploadFileFlux = null;
    final FileFluxCompleteHandler backupCompleteHandler = () -> this.backupFileFlux = null;

    protected Mono<String> deleteFile(File parent, String relativePath) {
        return Mono.fromCallable(() -> {
            File dfile = new File(parent, relativePath);
            if(dfile.exists()) {
                FileUtils.delete(dfile);
            }
            return relativePath;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    protected void clearEmptyFolders(File folder) {
        List<File> folders = (List<File>) FileUtils.listFilesAndDirs(folder, new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
        Collections.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int io1 = StringUtils.countMatches(o1.getAbsolutePath(), File.separatorChar);
                int io2 = StringUtils.countMatches(o2.getAbsolutePath(), File.separatorChar);
                return Integer.compare(io2, io1);
            }
        });
        folders.removeIf(f -> f.getAbsolutePath().equalsIgnoreCase(folder.getAbsolutePath()));
        Flux.fromStream(folders.stream())
                .flatMapSequential(this::deleteFolder)
                .doOnError(throwable -> {
                })
                .doOnComplete(() -> {
                    LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "Deleted all empty folders");
                })
                .blockLast();
        LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "*** Deleted all empty folders inside folder " + folder.getAbsolutePath());
    }

    protected Mono<String> clearEmptyFolders(File folder, String relativePath) {
        return Mono.fromCallable(() -> {
            File cFolder = new File(folder, relativePath).getParentFile();
            List<File> folders = (List<File>) FileUtils.listFilesAndDirs(cFolder, new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
            Collections.sort(folders, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    int io1 = StringUtils.countMatches(o1.getAbsolutePath(), File.separatorChar);
                    int io2 = StringUtils.countMatches(o2.getAbsolutePath(), File.separatorChar);
                    return Integer.compare(io2, io1);
                }
            });
            folders.removeIf(f -> f.getAbsolutePath().equalsIgnoreCase(folder.getAbsolutePath()));
            for(File dFolder : folders) {
                deleteFolder(dFolder).subscribe();
            }
            LoggerUtility.log(BackupState.class, LoggerUtility.LogLevel.INFO, "*** Deleted all empty folders inside folder " + cFolder.getAbsolutePath());
            return relativePath;
        }).subscribeOn(Schedulers.single());
    }

    protected Mono<File> deleteFolder(File file) {
        return Mono.fromCallable(() -> {
            if(FileUtils.isEmptyDirectory(file)) {
                FileUtils.deleteDirectory(file);
            }
            return file;
        }).subscribeOn(Schedulers.single());
    }

    public void shutdownGracefully() {
        if(!compressExecutorService.isShutdown()) {
            compressExecutorService.shutdown();
        }
        if(!uploadExecutorService.isShutdown()) {
            uploadExecutorService.shutdown();
        }
        if(!backupExecutorService.isShutdown()) {
            backupExecutorService.shutdown();
        }
        if(!moveImageScheduler.isDisposed()) {
            moveImageScheduler.disposeGracefully();
        }
        if(!extractJsonScheduler.isDisposed()) {
            extractJsonScheduler.disposeGracefully();
        }
        if(!modifyScheduler.isDisposed()) {
            modifyScheduler.disposeGracefully();
        }
    }
}
