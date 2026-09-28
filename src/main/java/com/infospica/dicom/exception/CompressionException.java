package com.infospica.dicom.exception;

public class CompressionException extends Exception {

    public CompressionException(String message) {
        super(message);
    }

    public CompressionException(String message, Throwable t) {
        super(message, t);
    }
}
