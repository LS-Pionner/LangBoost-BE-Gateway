package com.example.cloudroute.response;

import com.example.api.response.ErrorCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeInterface {

    // 401 Unauthorized
    MISSING_AUTHORIZATION_HEADER(40101, HttpStatus.UNAUTHORIZED, "Authorization header is missing or incorrect"),
    INVALID_TOKEN(40102, HttpStatus.UNAUTHORIZED, "Token is not valid");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}