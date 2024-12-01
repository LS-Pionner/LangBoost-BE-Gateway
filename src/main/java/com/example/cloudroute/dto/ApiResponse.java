package com.example.cloudroute.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        @JsonIgnore HttpStatus httpStatus,
        boolean success,
        @Nullable T payload,
        @Nullable String error
) {

}

