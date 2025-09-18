package com.record.myprivateproject.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID = "traceId";

    private ResponseEntity<Object> build(ErrorCode errorCode, HttpServletRequest request, String detail, List<ApiErrorResponse.FieldError> errors) {
        String traceId = MDC.get(TRACE_ID);
        ApiErrorResponse body = ApiErrorResponse.of(errorCode, request, detail, errors, traceId);
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 1) 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusiness(BusinessException e, HttpServletRequest request) {
        return build(e.getErrorCode(), request, e.getMessage(), null);
    }

    // 2) @Valid DTO 바인딩 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = new ArrayList<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.add(ApiErrorResponse.FieldError.of(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
        }
        return build(ErrorCode.VALIDATION_FAILED, request, "요청 본문 검증 실패", errors);
    }

    // 3) @ModelAttribute / QueryParam 바인딩 실패
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<Object> handleBindException(BindException e, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = new ArrayList<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.add(ApiErrorResponse.FieldError.of(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
        }
        return build(ErrorCode.INVALID_INPUT_VALUE, request, "요청 파라미터 바인딩 실패", errors);
    }

    // 4) @Validated 메서드 파라미터(Controller) 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<ApiErrorResponse.FieldError> errors = new ArrayList<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(ApiErrorResponse.FieldError.of(v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()));
        }
        return build(ErrorCode.VALIDATION_FAILED, request, "제약 조건 위반", errors);
    }

    // 5) JSON 파싱/형 변환
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        String detail = "본문 파싱 실패";
        if (e.getCause() instanceof InvalidFormatException ife) {
            detail = "값 형식이 올바르지 않습니다: " + ife.getValue();
        }
        return build(ErrorCode.JSON_PARSE_ERROR, request, detail, null);
    }

    // 6) 미지원 미디어 타입
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return build(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED, request, e.getMessage(), null);
    }

    // 7) HTTP Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<Object> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, request, e.getMessage(), null);
    }

    // 8) 필수 파라미터/경로 변수 누락
    @ExceptionHandler({ MissingServletRequestParameterException.class, MissingPathVariableException.class })
    protected ResponseEntity<Object> handleMissingParam(Exception e, HttpServletRequest request) {
        return build(ErrorCode.INVALID_INPUT_VALUE, request, e.getMessage(), null);
    }

    // 9) 접근 거부 (스프링 시큐리티)
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(ErrorCode.HANDLE_ACCESS_DENIED, request, e.getMessage(), null);
    }

    // 10) 데이터 무결성/유니크 제약
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return build(ErrorCode.CONFLICT_STATE, request, "데이터 무결성 제약 위반", null);
    }

    // 11) 스프링의 ErrorResponseException (예: 404 변환 등)
    @ExceptionHandler(ErrorResponseException.class)
    protected ResponseEntity<Object> handleErrorResponse(ErrorResponseException e, HttpServletRequest request) {
        HttpStatusCode status = e.getStatusCode();
        ErrorCode code = (status.is4xxClientError()) ? ErrorCode.ENTITY_NOT_FOUND : ErrorCode.INTERNAL_SERVER_ERROR;
        return build(code, request, e.getMessage(), null);
    }

    // 12) 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object > handleException(Exception e, HttpServletRequest request) {
        // 로깅은 AOP/필터에서 추적, 여기서도 에러 로그 남겨도 OK
        return build(ErrorCode.INTERNAL_SERVER_ERROR, request, e.getMessage(), null);
    }
}
