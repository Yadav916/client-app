package com.infospica.dicom.command;

import com.infospica.dicom.command.CommandExecution;
import com.infospica.dicom.command.exception.UploadCommandException;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class EchoServerCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public EchoServerCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine();
        String response = executeCommandWithResult(cmdLine);
        String statusCompare = "Connected to " + Context.getStoreSCUConfigurationProperties().getAetName();
        if (response.contains(statusCompare))
            return "SUCCESS";
        return "ERROR";
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    private CommandLine getPreparedCommandLine() {
        if(this.commandLine == null) {
            this.commandLine = new CommandLine(getExecutableCommand("storescu"));
            String bindInfo;
            if((bindInfo = getBind()) != null) {
                this.commandLine.addArgument("-b");
                this.commandLine.addArgument(bindInfo);
            }
            this.commandLine.addArgument("-c");
            this.commandLine.addArgument(getHost());
        }
        return this.commandLine;
    }

    private String getHost() {
        String hdef = Context.getStoreSCUConfigurationProperties().getAetName();
        if(Context.getStoreSCUConfigurationProperties().getHostName() != null && !Context.getStoreSCUConfigurationProperties().getHostName().trim().isEmpty()) {
            hdef = hdef + "@" + Context.getStoreSCUConfigurationProperties().getHostName();
        }
        return hdef + ":" + Context.getStoreSCUConfigurationProperties().getPort();
    }

    private String getBind() {
        String bindInfo = Context.getStoreSCUConfigurationProperties().getCallingAetName();
        if(bindInfo == null || bindInfo.trim().isEmpty()) return null;
        if(Context.getStoreSCUConfigurationProperties().getCallingHost() != null && !Context.getStoreSCUConfigurationProperties().getCallingHost().trim().isEmpty()) {
            bindInfo =  bindInfo + "@" + Context.getStoreSCUConfigurationProperties().getCallingHost();
        }
        return bindInfo;
    }
}
