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

public class EchoSCPCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public EchoSCPCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine();
        int exitCode = executeCommand(cmdLine, true, 5000, l -> {
            if(!Context.skipLogCreation()) {
                try {
                    File logFile = new File(Context.getSystemLogFolder(), "scp-" + Constants._SCP_LOG_FILE_FORMAT.format(new Date()) + ".log");
                    FileUtils.writeStringToFile(logFile, l + "\r\n", "UTF-8", true);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        if (exitCode > 0) return "ERROR";
        return "SUCCESS";
    }

    private CommandLine getPreparedCommandLine() {
        if(this.commandLine == null) {
            this.commandLine = new CommandLine(getExecutableCommand("storescu"));
            this.commandLine.addArgument("-c");
            this.commandLine.addArgument(getHost());
        }
        return this.commandLine;
    }

    private String getHost() {
        String hdef = Context.getStoreSCPConfigurationProperties().getAetName();
        if(Context.getStoreSCPConfigurationProperties().getHostName() != null && !Context.getStoreSCPConfigurationProperties().getHostName().trim().isEmpty()) {
            hdef = hdef + "@" + Context.getStoreSCPConfigurationProperties().getHostName();
        } else {
            hdef = hdef + "@localhost";
        }
        return hdef + ":" + Context.getStoreSCPConfigurationProperties().getPort();
    }


}
