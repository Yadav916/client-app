package com.infospica.dicom.exception;

public class ModificationException extends Exception {

    public ModificationException(String message) {
        super(message);
    }

    public ModificationException(String message, Throwable t) {
        super(message, t);
    }
}
