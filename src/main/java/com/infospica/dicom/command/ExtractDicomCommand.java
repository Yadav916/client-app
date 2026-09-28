package com.infospica.dicom.command;

import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.exception.ModificationException;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExtractDicomCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public ExtractDicomCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public String execute(Object... args) throws Exception {
        CommandLine cmdLine = getPreparedCommandLine();
        @SuppressWarnings("unchecked")
        Map<String, String> submap = (HashMap<String, String>)cmdLine.getSubstitutionMap();
        submap.clear();
        submap.put("filepath", (String)args[0]);
        return executeCommandWithResult(cmdLine);
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    private CommandLine getPreparedCommandLine() {
        //https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-dcm2json/README.md
        if(this.commandLine == null) {
            this.commandLine = new CommandLine(getExecutableCommand("dcm2json"));
            this.commandLine.addArgument("-B");
            this.commandLine.addArgument("${filepath}");
            this.commandLine.setSubstitutionMap(new HashMap<String, String>());
        }
        return this.commandLine;
    }
}
