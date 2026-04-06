package com.alber.outlookdesktop.service;

public class OutlookComException extends RuntimeException {

    public OutlookComException(String message) {
        super(message);
    }

    public OutlookComException(String message, Throwable cause) {
        super(message, cause);
    }
}
