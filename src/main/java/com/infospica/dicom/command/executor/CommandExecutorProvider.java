package com.infospica.dicom.command.executor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface CommandExecutorProvider {
    default String getIdentifier() {
        return UUID.randomUUID().toString();
    }
    void applyWorkingDirectory(File workingDirectory);
    String executeCommand(CommandLine commandLine) throws ExecuteException, IOException;
    void executeCommand(CommandLine commandLine, long waitTime) throws IOException, InterruptedException;
    int getExitValue();
    String getOutputString();
    void release();
}
