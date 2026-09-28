package com.infospica.dicom.command.exception;

import org.apache.commons.io.FileExistsException;

import java.io.File;

public class DicomFileExistsException extends FileExistsException {

    private File existFile;

    public DicomFileExistsException(File existFile) {
        super();
        this.existFile = existFile;
    }

    public File getExistingFile() {
        return existFile;
    }
}
