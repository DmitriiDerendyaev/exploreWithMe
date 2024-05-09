package org.example.exception;

public class RulesViolationException extends RuntimeException {
    public RulesViolationException(String message) {
        super(message);
    }
}