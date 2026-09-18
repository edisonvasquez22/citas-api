package com.fcv.citas.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ApiError(Instant timestamp, int status, String error, String message, List<String> detalles) {

    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, List.of());
    }

    public static ApiError of(HttpStatus status, String message, List<String> detalles) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, detalles);
    }
}
