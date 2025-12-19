package com.brasens.utilities.common.enums;

public enum HttpStatusCode {
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    ACCEPTABLE(202, "Acceptable");

    private final int code;
    private final String message;

    HttpStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static HttpStatusCode fromCode(int code) {
        for (HttpStatusCode httpStatusCode : values()) {
            if (httpStatusCode.code == code) {
                return httpStatusCode;
            }
        }
        return null;
    }
}
