package com.xxx.cloud.auth.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationServerException extends SystemException {

    private final HttpStatus status;

    public AuthorizationServerException(HttpStatus status, String error, String description) {
        super(error, description);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
