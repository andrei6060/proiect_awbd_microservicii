package com.clinic.clinic.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    INCORRECT_PASSWORD(300, BAD_REQUEST, "Incorrect password"),
    NEW_PASSWORD_DOESNT_MATCH(400, BAD_REQUEST, "New password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "USER ACCOUNT IS LOCKED"), //de sters
    ACCOUNT_DISABLED(303, FORBIDDEN, "USER ACCOUNT DISABLED"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Bad credentials"),

    ;
    @Getter
    private final int code;
    @Getter
    private final String message;
    @Getter
    private final HttpStatus httpStatus;

    ErrorCodes(int code,  HttpStatus httpStatus, String message) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
