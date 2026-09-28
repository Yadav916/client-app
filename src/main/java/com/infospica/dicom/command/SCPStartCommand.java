package com.infospica.dicom.command;

import com.infospica.dicom.Constants;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.EchoServer;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SCPStartCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public SCPStartCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine(Context.getMachineDestinationFolder().getAbsolutePath());
        @SuppressWarnings("unchecked")
        Map<String, String> submap = (HashMap<String, String>)cmdLine.getSubstitutionMap();
        if(submap != null) {
            submap.clear();
            submap.put("filepath", Context.getMachineDestinationFolder().getAbsolutePath());
        }
        int exitCode = executeUnstoppableCommand(cmdLine, true, 5000, l -> {
            if(!Context.skipLogCreation()) {
                try {
                    File logFile = new File(Context.getSystemLogFolder(), "scp-" + Constants._SCP_LOG_FILE_FORMAT.format(new Date()) + ".log");
                    FileUtils.writeStringToFile(logFile, l + "\r\n", "UTF-8", true);
                } catch (IOException ioe) {
                }
            }
        });

        if(exitCode > 0) return "ERROR";
        return "SUCCESS";
    }

    private CommandLine getPreparedCommandLine(String destFolder) {
        //https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-deidentify/README.md
        //https://cloud.google.com/healthcare-api/docs/how-tos/dicom-deidentify //
        if(this.commandLine == null) {
            String preparedCommand = prepareCommand(destFolder);
            if(preparedCommand != null) {
                this.commandLine = new CommandLine(preparedCommand);
            } else {
                this.commandLine = new CommandLine(getExecutableCommand("storescp"));
                this.commandLine.addArgument("-b");
                this.commandLine.addArgument(getHost());
                this.commandLine.addArgument("--directory");
                this.commandLine.addArgument("${filepath}");
                this.commandLine.addArgument("--request-timeout");
                this.commandLine.addArgument(Context.getConfigurationContext().getSCPRequestTimeout());
                this.commandLine.addArgument("--release-timeout");
                this.commandLine.addArgument(Context.getConfigurationContext().getSCPReleaseTimeout());
                this.commandLine.setSubstitutionMap(new HashMap<String, String>());
            }
        }
        return this.commandLine;
    }

    private String prepareCommand(String...params) {
        StringBuilder sb = new StringBuilder();
        if(SystemUtils.IS_OS_WINDOWS) {
            sb.append("start \"SCP-Service\" /SEPARATE /B").append(" ");
        } else {
            sb.append("nohup").append(" ");
        }
        sb.append(getExecutableCommand("storescp")).append(" ")
                .append("-b").append(" ")
                .append(getHost()).append(" ")
                .append("--directory").append(" ")
                .append(params[0]).append(" ")
                .append("--request-timeout").append(" ")
                .append(Context.getConfigurationContext().getSCPRequestTimeout()).append(" ")
                .append("--release-timeout").append(" ")
                .append(Context.getConfigurationContext().getSCPReleaseTimeout()).append(" ");
        if(SystemUtils.IS_OS_LINUX) {
            sb.append("> /dev/null 2>&1").append(" ").append("&");
        }
        try {
            return storeCommandAsFile("startscp", sb);
        } catch (IOException e) {
            LoggerUtility.log(SCPStartCommand.class, "Error while rewriting command.", e);
        }
        return null;
    }

    private String getHost() {
        String hdef = Context.getStoreSCPConfigurationProperties().getAetName();
        if(Context.getStoreSCPConfigurationProperties().getHostName() != null && !Context.getStoreSCPConfigurationProperties().getHostName().trim().isEmpty()) {
            hdef = hdef + "@" + Context.getStoreSCPConfigurationProperties().getHostName();
        }
        return hdef + ":" + Context.getStoreSCPConfigurationProperties().getPort();
    }


}
