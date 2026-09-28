package com.infospica.dicom.command.pool;

import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.executor.ToolkitCommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class ToolkitExecutorPool extends ExecutableCommandPool<CommandExecutorProvider> {

    public ToolkitExecutorPool() {
        super();
        createPool();
    }

    @Override
    protected CommandExecutorProvider createCommand() {
        return new ToolkitCommandExecutor();
    }
}
