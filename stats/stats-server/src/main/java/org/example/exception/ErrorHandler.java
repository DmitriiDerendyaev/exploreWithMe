package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerStatsValidationException(final StatsValidationException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse("Validation for statistic failed: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerStatsValidationException(final MissingServletRequestParameterException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse("Missing parameters: ", e.getMessage());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerInternalServerError(final Throwable e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse("Server error: ", e.getMessage());
    }
}
