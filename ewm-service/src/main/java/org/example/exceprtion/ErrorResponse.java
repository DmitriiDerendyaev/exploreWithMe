package org.example.exceprtion;

import lombok.Data;

@Data
public class ErrorResponse {
    String error;
    String cause;

    public ErrorResponse(String error, String cause) {
        this.error = error;
        this.cause = cause;
    }
}
