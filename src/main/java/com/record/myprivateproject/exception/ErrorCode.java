package com.record.myprivateproject.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // === Common (1000) ===
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "파라미터 타입이 올바르지 않습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근 권한이 없습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "C007", "요청 본문(JSON) 파싱 오류입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C008", "지원하지 않는 미디어 타입입니다."),
    NOT_READABLE_BODY(HttpStatus.BAD_REQUEST, "C009", "읽을 수 없는 요청 본문입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "C010", "요청이 너무 많습니다."),
    CONFLICT_STATE(HttpStatus.CONFLICT, "C011", "리소스 상태 충돌이 발생했습니다."),
    VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "C012", "검증에 실패했습니다."),

    // === Auth (2000) ===
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A003", "토큰이 유효하지 않습니다."),

    // === Domain (3000~) 프로젝트별 확장
    SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "요청한 시트가 존재하지 않습니다."),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D002", "파일 저장 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
