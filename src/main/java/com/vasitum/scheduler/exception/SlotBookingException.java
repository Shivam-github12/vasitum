package com.vasitum.scheduler.exception;

public class SlotBookingException extends RuntimeException {
    public SlotBookingException(String message) {
        super(message);
    }

    public SlotBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}