package com.infospica.dicom.command.pool;

import com.infospica.dicom.Constants;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class ExtractDicomCommandPool extends ExecutableCommandPool<ExecutableCommand> {

    private final ApplicationContext applicationContext;

    public ExtractDicomCommandPool(final ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
        createPool();
    }

    public String execute(File file) throws Exception {
        ExecutableCommand command = acquire();
        LoggerUtility.log(ExtractDicomCommandPool.class, LoggerUtility.LogLevel.DEBUG, "ExtractDicom command instance: " + command.toString());
        try {
            return command.execute(file.getAbsolutePath());
        } finally {
            recycle(command);
        }
    }

    public void resetAll() {
        removeAll();
        createPool();
    }

    @Override
    protected ExecutableCommand createCommand() {
        return ((ExecutableCommand)applicationContext.getBean(Constants.EXTRACT_COMMAND_BEAN_NAME));
    }
}
