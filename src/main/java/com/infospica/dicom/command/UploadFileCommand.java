package com.infospica.dicom.command;

import com.infospica.dicom.command.exception.UploadCommandException;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.exception.ModificationException;
import org.apache.commons.exec.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class UploadFileCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public UploadFileCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine();

        @SuppressWarnings("unchecked")
        Map<String, String> submap = (HashMap<String, String>) cmdLine.getSubstitutionMap();
        submap.clear();
        submap.put("filepath", (String) args[0]);

        int exitCode = executeCommand(cmdLine);
        if (exitCode > 0) throw new UploadCommandException("Error uploading the file: " + args[0]);
        return (String) args[0];
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    private CommandLine getPreparedCommandLine() {
        //old command
        //storescu demo.eclarityhealth.com 11112 "c:\eClarity\Temp\Pending" +r +sd -aec DCM4CHEE -nj --repeat 1 -to 10 -ts 10 -ta 10 -td 10
        // +m -ll info -aet BASPACS > C:\eClarity\PACSClientTurbo\Logs\StoreSCU\2023102920222.log

        // new command
        //storescu -c STORESCP@localhost:11112 image.dcm
        if(this.commandLine == null) {
            this.commandLine = new CommandLine(getExecutableCommand("storescu"));
            String bindInfo;
            if((bindInfo = getBind()) != null) {
                this.commandLine.addArgument("-b");
                this.commandLine.addArgument(bindInfo);
            }
            this.commandLine.addArgument("-c");
            this.commandLine.addArgument(getHost());
            this.commandLine.addArgument("${filepath}");
            this.commandLine.setSubstitutionMap(new HashMap<String, String>());
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
