package com.xxx.cloud.auth.exception;

import java.util.List;

public class SystemException extends RuntimeException {

    static final String DEFAULT_INTERNAL_SERVER_ERROR = "internal_error";
    static final String DEFAULT_INTERNAL_SERVER_ERROR_DESCRIPTION = "Error occurred while handling requests.";

    private final String error;
    private final String description;
    private final List<Object> details;

    public SystemException(String error, String description) {
        this(error, description, null);
    }

    public SystemException(String error, String description, List<Object> details) {
        this.error = error;
        this.description = description;
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public List<Object> getDetails() {
        return details;
    }

    public static SystemException create() {
        return new SystemException(DEFAULT_INTERNAL_SERVER_ERROR, DEFAULT_INTERNAL_SERVER_ERROR_DESCRIPTION);
    }

    public static SystemException create(String description) {
        return new SystemException(DEFAULT_INTERNAL_SERVER_ERROR, description);
    }

    public static SystemException create(String error, String description) {
        return new SystemException(error, description);
    }

}
