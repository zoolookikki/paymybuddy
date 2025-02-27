package com.cordierlaurent.paymybuddy.exception;

public class CurrentUserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CurrentUserNotFoundException(String message) {
        super(message);
    }
}
