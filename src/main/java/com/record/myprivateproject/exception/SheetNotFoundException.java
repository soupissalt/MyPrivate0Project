package com.record.myprivateproject.exception;

public class SheetNotFoundException extends BusinessException {
    public SheetNotFoundException(String details) {
        super(ErrorCode.SHEET_NOT_FOUND, details);
    }
}
