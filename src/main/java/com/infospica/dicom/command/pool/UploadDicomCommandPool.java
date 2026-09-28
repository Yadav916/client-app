package com.infospica.dicom.command.pool;

import com.infospica.dicom.Constants;
import com.infospica.dicom.config.iface.ExecutableCommand;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class UploadDicomCommandPool extends ExecutableCommandPool<ExecutableCommand> {

    private final ApplicationContext applicationContext;

    public UploadDicomCommandPool(final ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
        createPool();
    }

    public String execute(File file) throws Exception {
        ExecutableCommand command = acquire();
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
        return ((ExecutableCommand)applicationContext.getBean(Constants.UPLOAD_COMMAND_BEAN_NAME));
    }
}
