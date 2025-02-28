package com.cordierlaurent.paymybuddy.exception;

// Toute erreur interne concernant un utilisateur qui n'existe plus dans la base de données.
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }
}
