package org.example.exception;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String error;
    private final String cause;
}
