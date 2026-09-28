package com.infospica.dicom.command;

import com.infospica.dicom.Constants;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.EchoServer;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SCPStopCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public SCPStopCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine();
        int exitCode = executeCommand(cmdLine, true, 0, l -> {
            if(!Context.skipLogCreation()) {
                try {
                    File logFile = new File(Context.getSystemLogFolder(), "scp-" + Constants._SCP_LOG_FILE_FORMAT.format(new Date()) + ".log");
                    FileUtils.writeStringToFile(logFile, l + "\r\n\r\n", "UTF-8", true);
                } catch (IOException ioe) {
                }
            }
        });
        if(exitCode > 0) return "ERROR";
        return "SUCCESS";
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    private CommandLine getPreparedCommandLine() {
        if(this.commandLine == null) {
            this.commandLine = new CommandLine(getBatchFile("killscp"));
            this.commandLine.addArgument(String.valueOf(Context.getStoreSCPConfigurationProperties().getPort()));
        }
        return this.commandLine;
    }

    public String getBatchFile(String command) {
        return new File(Context.getConfigurationContext().getCommandWorkingFolder()).getAbsolutePath()
                + File.separator + command + Constants.SCRIPT_COMMAND_EXTENSION;
    }
}
