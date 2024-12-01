package com.example.cloudroute.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode{

    // 400 BAD REQUEST
    INVALID_REQUEST(40001, HttpStatus.BAD_REQUEST, "Invalid request format or processing error."),

    // 401 Unauthorized
    MISSING_AUTHORIZATION_HEADER(40101, HttpStatus.UNAUTHORIZED, "Authorization header is missing or incorrect"),
    INVALID_TOKEN(40102, HttpStatus.UNAUTHORIZED, "Token is not valid"),
    MISSING_VERIFY_RESULT(40103, HttpStatus.UNAUTHORIZED, "VerifyResult not found in exchange attributes."),
    AUTHENTICATION_FAILED(40104, HttpStatus.UNAUTHORIZED, "Authentication failed for the provided VerifyResult.");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}