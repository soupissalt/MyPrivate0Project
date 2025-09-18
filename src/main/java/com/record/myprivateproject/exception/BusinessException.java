package com.record.myprivateproject.exception;

public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String details;

    protected BusinessException(ErrorCode errorCode, String details) {
        super(details);
        this.errorCode = errorCode;
        this.details = details;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    public String getDetails() {
        return details;
    }
}
