package com.infospica.dicom.command.pool;

import com.infospica.dicom.Constants;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.process.batch.pool.SimpleUploadTaskPool;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class SimpleUploadDicomCommandPool extends ExecutableCommandPool<ExecutableCommand> {

    private final ApplicationContext applicationContext;

    public SimpleUploadDicomCommandPool(final ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
        createPool();
    }

    public String execute(File file) throws Exception {
        ExecutableCommand command = acquire();
        LoggerUtility.log(SimpleUploadDicomCommandPool.class, LoggerUtility.LogLevel.DEBUG, "Upload command instance: " + command.toString());
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
        return ((ExecutableCommand)applicationContext.getBean(Constants.SIMPLE_UPLOAD_COMMAND_BEAN_NAME));
    }
}
