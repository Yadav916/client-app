package com.infospica.dicom.command.executor;

import com.infospica.dicom.command.handler.SimplePumpStreamHandler;
import com.infospica.dicom.context.Context;
import org.apache.commons.exec.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ToolkitCommandExecutor implements CommandExecutorProvider {
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final DefaultExecutor exec = new DefaultExecutor();
    private final SimplePumpStreamHandler streamHandler = new SimplePumpStreamHandler(stdout);

    private DefaultExecuteResultHandler resultHandler = null;
    private String identifier = UUID.randomUUID().toString();

    public ToolkitCommandExecutor() {
        exec.setExitValue(0);
        exec.setStreamHandler(streamHandler);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void applyWorkingDirectory(File workingDirectory) {
        exec.setWorkingDirectory(workingDirectory);
    }

    @Override
    public String executeCommand(CommandLine commandLine) throws ExecuteException, IOException {
        exec.execute(commandLine);
        return stdout.toString();
    }

    @Override
    public void executeCommand(CommandLine commandLine, long waitTime) throws IOException, InterruptedException {
        resultHandler = new DefaultExecuteResultHandler();
        exec.execute(commandLine, resultHandler);
        if(waitTime > 0) resultHandler.waitFor(waitTime);
        else resultHandler.waitFor(20000L); //default wait time; 20sec
    }

    @Override
    public int getExitValue() {
        if(resultHandler != null) {
            try {
                return resultHandler.getExitValue();
            } catch(IllegalStateException is) {}
        }
        return 0;
    }


    @Override
    public String getOutputString() {
        return stdout.toString();
    }

    @Override
    public void release() {
        try {
            stdout.close();
            stdout.reset();
        } catch (IOException ioe) { } finally {
            resultHandler = null;
        }
    }
}
