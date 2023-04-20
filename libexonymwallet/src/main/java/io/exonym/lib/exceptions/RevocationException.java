package io.exonym.lib.exceptions;

public class RevocationException extends Exception {

    public RevocationException() {
    }

    public RevocationException(String message) {
        super(message);
    }

    public RevocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
