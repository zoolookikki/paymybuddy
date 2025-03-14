package com.cordierlaurent.paymybuddy.util;

import lombok.ToString;

/**
 * Class representing the result of an operation.
 * <p>
 * This class encapsulates the success or failure status of an operation, as well as an explanatory message.
 * </p>
 */
@ToString
public class Result {
    private boolean success;
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
