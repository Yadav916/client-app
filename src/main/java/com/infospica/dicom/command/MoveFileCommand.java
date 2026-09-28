package com.infospica.dicom.command;

import com.infospica.dicom.command.exception.DicomFileExistsException;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.FileUtility;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.StandardCopyOption;

public class MoveFileCommand implements ExecutableCommand {

    public MoveFileCommand() {
    }

    @Override
    public String execute(Object... args) throws Exception {
        if(args.length < 1) return "ERROR";
        File srcFile = (File)args[0];
        String maskPath = (String)args[1];
        File destFile = (File)args[2];
        Boolean randomize = Boolean.FALSE;
        if(args.length == 4) {
            randomize = (Boolean)args[3];
        }
        File processFile = FileUtility.newDestinationFile(srcFile, maskPath, destFile, randomize);
        try {
            FileUtils.moveFile(srcFile, processFile, StandardCopyOption.REPLACE_EXISTING);
            //FileUtils.copyFile(srcFile, processFile);
            return processFile.getAbsolutePath();
        } catch (Exception e) {
            throw new DicomFileExistsException(processFile);
        }
    }
}
