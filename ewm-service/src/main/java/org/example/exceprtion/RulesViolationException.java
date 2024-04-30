package org.example.exceprtion;

public class RulesViolationException extends RuntimeException {
    public RulesViolationException(String message) {
        super(message);
    }
}