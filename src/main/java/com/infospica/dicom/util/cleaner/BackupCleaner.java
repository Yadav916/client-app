package com.infospica.dicom.util.cleaner;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class BackupCleaner {

    public void clean(final int keepDays) {
        Context.setBackupCleanStarted(true);
        Scheduler scheduler = Schedulers.boundedElastic();
        Flux.create((FluxSink<File> fluxSink) -> {
            List<File> filesToDelete = new ArrayList<>();

            filesToDelete.addAll(getProcessedBackupFiles(keepDays));
            //filesToDelete.addAll(getAppErrorLogFiles(keepDays));
            //filesToDelete.addAll(getAppInfoLogFiles(keepDays));
            //filesToDelete.addAll(getAnonymizedLogFiles(keepDays));
            //filesToDelete.addAll(getCompressedLogFiles(keepDays));
            //filesToDelete.addAll(getUploadLogFiles(keepDays));
            filesToDelete.addAll(getScpLogFiles(keepDays));

            filesToDelete.stream().forEach(fluxSink::next);
            fluxSink.complete();
        }).parallel().runOn(scheduler).flatMap(f -> {
            try {
                if(f.isDirectory()) {
                    FileUtils.deleteDirectory(f);
                } else {
                    FileUtils.delete(f);
                }
            } catch (IOException e) {
                LoggerUtility.log(BackupCleaner.class, LoggerUtility.LogLevel.ERROR, "Error cleaning backup: " + e.getMessage());
            }
            return Mono.empty();
        }).sequential()
        .doOnComplete(() -> {
            LoggerUtility.log(BackupCleaner.class, LoggerUtility.LogLevel.INFO, "Backup clean complete");
        })
        .doOnError(t -> LoggerUtility.log(BackupCleaner.class, LoggerUtility.LogLevel.INFO, "Error clean backup. Cause: " + t.getMessage()))
        .blockLast();
        if(!scheduler.isDisposed()) {
            scheduler.disposeGracefully();
        }
        Context.setBackupCleanStarted(false);
    }
    private List<File> getProcessedBackupFiles(int keepDays) {
        File folder = Context.getUploadBackupFolder();
        File[] files = folder.listFiles(new FileNameWithTimeFilter(keepDays));
        if(files == null) return Collections.emptyList();
        return Arrays.asList(files);
    }

    private List<File> getAppErrorLogFiles(int keepDays) {
        File logFolder = new File(Context.getSystemLogFolder(), "archived-logs");
        File[] errorLogFiles = logFolder.listFiles(new LogFileNameWithTimeFilter("app-error-logger-", keepDays));
        if(errorLogFiles == null) return Collections.emptyList();
        return Arrays.asList(errorLogFiles);
    }

    private List<File> getAppInfoLogFiles(int keepDays) {
        File logFolder = new File(Context.getSystemLogFolder(), "archived-logs");
        File[] infoLogFiles = logFolder.listFiles(new LogFileNameWithTimeFilter("app-info-logger-", keepDays));
        if(infoLogFiles == null) return Collections.emptyList();
        return Arrays.asList(infoLogFiles);
    }

    private List<File> getAnonymizedLogFiles(int keepDays) {
        File logFolder = new File(Context.getSystemLogFolder(), "archived-logs/anonymize");
        File[] infoLogFiles = logFolder.listFiles(new LogFileNameWithTimeFilter("Anonymize-logFile-", keepDays));
        if(infoLogFiles == null) return Collections.emptyList();
        return Arrays.asList(infoLogFiles);
    }

    private List<File> getCompressedLogFiles(int keepDays) {
        File logFolder = new File(Context.getSystemLogFolder(), "archived-logs/compress");
        File[] infoLogFiles = logFolder.listFiles(new LogFileNameWithTimeFilter("Compress-logFile-", keepDays));
        if(infoLogFiles == null) return Collections.emptyList();
        return Arrays.asList(infoLogFiles);
    }

    private List<File> getUploadLogFiles(int keepDays) {
        File logFolder = new File(Context.getSystemLogFolder(), "archived-logs/upload");
        File[] infoLogFiles = logFolder.listFiles(new LogFileNameWithTimeFilter("Upload-logFile-", keepDays));
        if(infoLogFiles == null) return Collections.emptyList();
        return Arrays.asList(infoLogFiles);
    }

    private List<File> getScpLogFiles(int keepDays) {
        File[] infoLogFiles = Context.getSystemLogFolder().listFiles(new LogFileNameWithTimeFilter("scp-", keepDays));
        if(infoLogFiles == null) return Collections.emptyList();
        return Arrays.asList(infoLogFiles);
    }
}
