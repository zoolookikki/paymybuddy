package com.cordierlaurent.paymybuddy.exception;

// Toute erreur interne concernant une transaction.
public class TransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TransactionException(String message) {
        super(message);
    }
}
