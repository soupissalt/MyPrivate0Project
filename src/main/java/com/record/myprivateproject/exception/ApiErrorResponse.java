package com.record.myprivateproject.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private String code;
    private String message;
    private String details;
    private String path;
    private String timestamp;
    private List<FieldError> errors;
    private String traceId;

    public static ApiErrorResponse of(ErrorCode errorCode, String details, String path, List<FieldError> errors,String traceId) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                details,
                path,
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                errors,
                traceId
        );
    }
    public static ApiErrorResponse of(ErrorCode errorCode, HttpServletRequest req, String details, List<FieldError> errors,String traceId) {
        return of(errorCode, details, req != null ? req.getRequestURI() : null, errors, traceId);
    }

    public record FieldError(String field, Object value, String reason){
        public static FieldError of(String field, Object value, String reason) {
            return new FieldError(field, value, reason);
        }
    }

    private ApiErrorResponse(String code, String message, String details, String path, String timestamp, List<FieldError> errors, String traceId) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.path = path;
        this.timestamp = timestamp;
        this.errors = errors;
        this.traceId = traceId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getPath() {
        return path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public String getTraceId() {
        return traceId;
    }
}
