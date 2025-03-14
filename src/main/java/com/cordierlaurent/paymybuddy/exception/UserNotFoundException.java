package com.cordierlaurent.paymybuddy.exception;

/**
 * Any internal error regarding a user that no longer exists in the database.
 */
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }
}
