package com.infospica.dicom.process;

import com.infospica.dicom.command.exception.DicomFileExistsException;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

@Component
public class MoveTask {

    @Autowired
    private ApplicationContext applicationContext;

    public Mono<File> moveFileForProcessing(File srcFile, String maskPath, File destFile) {
        String processFile;
        try {
            processFile = ((ExecutableCommand)applicationContext.getBean("moveFileCommand"))
                    .execute(srcFile, maskPath, destFile);
            LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.DEBUG, processFile + "[Moved]");
            return Mono.just(new File(processFile));
        } catch (Exception e) {
            if(e instanceof DicomFileExistsException) {
                try {
                    FileUtils.delete(srcFile);
                    LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.WARN, "File " + srcFile.getAbsolutePath() + " already exists. [Skipped & Deleted]");
                } catch (IOException ex) {

                }
                return Mono.just(((DicomFileExistsException)e).getExistingFile());
            }
        }
        return Mono.empty();
    }

    public Mono<File> moveFileForProcessing(File srcFile, String maskPath, File destFile, Boolean randomize) {
        String processFile;
        try {
            processFile = ((ExecutableCommand)applicationContext.getBean("moveFileCommand"))
                    .execute(srcFile, maskPath, destFile, randomize);
            LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.DEBUG, processFile + "[Moved]");
            return Mono.just(new File(processFile));
        } catch (Exception e) {
            if(e instanceof DicomFileExistsException) {
                try {
                    FileUtils.delete(srcFile);
                    LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.WARN, "File " + srcFile.getAbsolutePath() + " already exists. [Skipped & Deleted]");
                } catch (IOException ex) {

                }
                return Mono.just(((DicomFileExistsException)e).getExistingFile());
            }
        }
        return Mono.empty();
    }
    public File moveFileForProcessing(File srcFile, String maskPath, File destFile, boolean flag) {
        String processFile;
        try {
            processFile = ((ExecutableCommand)applicationContext.getBean("moveFileCommand"))
                    .execute(srcFile, maskPath, destFile);
            LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.DEBUG, processFile + "[Moved]");
        } catch (Exception e) {
            LoggerUtility.log(MoveTask.class,  LoggerUtility.LogLevel.ERROR, "Error during move file: Cause" + e.getMessage());
            if(e instanceof FileExistsException) {
                try {
                    FileUtils.delete(srcFile);
                    LoggerUtility.log(MoveTask.class, LoggerUtility.LogLevel.WARN, "File already exists. [Skipped & Deleted]");
                } catch (IOException ex) {

                }
            }
            return null;
        }
        return new File(processFile);
    }
}
