package edu.tamu.scholars.middleware.export.exception;

public class UnauthorizedExportException extends RuntimeException {

    private static final long serialVersionUID = -928374692174698721L;

    public UnauthorizedExportException(String message) {
        super(message);
    }

}
