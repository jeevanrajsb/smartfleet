package com.smartfleet.ai.common.exception;

public class DeliveryNotFoundException extends ApiException {
    public DeliveryNotFoundException(Long deliveryId) {
        super("Delivery not found: " + deliveryId);
    }
}
