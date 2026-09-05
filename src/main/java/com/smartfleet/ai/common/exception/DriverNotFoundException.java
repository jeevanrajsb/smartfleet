package com.smartfleet.ai.common.exception;

public class DriverNotFoundException extends ApiException {
    public DriverNotFoundException(Long driverId) {
        super("Driver not found: " + driverId);
    }
}
