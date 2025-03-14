package com.cordierlaurent.paymybuddy.exception;

/**
 * Any internal error regarding a transaction.
 */
public class TransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TransactionException(String message) {
        super(message);
    }
}
