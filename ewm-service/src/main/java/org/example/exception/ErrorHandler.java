package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("Incorrectly made request.");
        return new ErrorResponse(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.warn("Integrity constraint has been violated.");
        return new ErrorResponse(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(final ObjectNotFoundException e) {
        log.warn("The required object was not found.");
        return new ErrorResponse(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleRulesViolationException(final RulesViolationException e) {
        log.warn("For the requested operation the conditions are not met.");
        return new ErrorResponse(HttpStatus.FORBIDDEN, "For the requested operation the conditions are not met.",
                e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleObjectAlreadyExistException(final ObjectAlreadyExistException e) {
        log.warn("Object with already exist");
        return new ErrorResponse(HttpStatus.CONFLICT, "Object with already exist",
                e.getMessage(), LocalDateTime.now());

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequestException(final InvalidRequestException e) {
        log.warn("Incorrectly made request.");
        return new ErrorResponse(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerInternalServerError(final Throwable e) {
        log.warn("Internal server error: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.", e.getMessage(),
                LocalDateTime.now());
    }
}
