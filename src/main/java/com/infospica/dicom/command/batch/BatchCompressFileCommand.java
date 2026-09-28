package com.infospica.dicom.command.batch;

import com.infospica.dicom.command.CommandExecution;
import com.infospica.dicom.command.ModifyDicomCommand;
import com.infospica.dicom.command.exception.CompressionCommandException;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BatchCompressFileCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

    private CommandLine commandLine;

    public BatchCompressFileCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        super(executorProvider);
    }

    @Override
    public String execute(Object... args) throws Exception {
        String compressedFile = getCompressedFile((String) args[0]);
        CommandLine cmdLine = getPreparedCommandLine();
        @SuppressWarnings("unchecked")
        Map<String, String> submap = (HashMap<String, String>) cmdLine.getSubstitutionMap();
        submap.clear();
        submap.put("filepath1", (String) args[0]);
        submap.put("filepath2", compressedFile);
        return executeCommandWithResult(cmdLine);
    }

    @Override
    public void reset() {
        this.commandLine = null;
    }

    private CommandLine getPreparedCommandLine() {
        if (this.commandLine == null) {
            this.commandLine = new CommandLine(getExecutableCommand("dcm2dcm"));
            if (Context.getCompressionMethod() != null) {
                this.commandLine.addArgument(Context.getCompressionMethod());
            }
            if ("--j2ki".equals(Context.getCompressionMethod())) {
                this.commandLine.addArgument("-Q");
                this.commandLine.addArgument("100");
            }
            this.commandLine.addArgument("--max-threads");
            this.commandLine.addArgument("7");
            this.commandLine.addArgument("${filepath1}");
            this.commandLine.addArgument("${filepath2}");
            this.commandLine.setSubstitutionMap(new HashMap<String, String>());
        }
        return this.commandLine;
    }

    private String getCompressedFile(String srcFile) {
        File compressedFile = new File(Context.getCompressDestinationFolder(),
                FileUtility.getRelativePathFrom(srcFile, Context.getCompressSourceFolder().getAbsolutePath()));
        if (!compressedFile.getParentFile().exists()) {
            compressedFile.getParentFile().mkdirs();
        }
        LoggerUtility.log(ModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "Considering the compressed folder: " + compressedFile.getParent());
        return compressedFile.getAbsolutePath();
    }
}
