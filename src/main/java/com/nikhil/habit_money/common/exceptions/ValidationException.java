package com.nikhil.habit_money.common.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
