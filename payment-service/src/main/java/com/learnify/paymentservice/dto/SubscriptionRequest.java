package com.learnify.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRequest {

    // FREE, MONTHLY, ANNUAL
    @NotBlank(message = "Plan is required (FREE, MONTHLY, ANNUAL)")
    private String plan;

    // Payment mode used to fund the subscription (null for FREE plan)
    private String paymentMode;

    // Mock transaction ID (null for FREE plan)
    private String transactionId;

    // Whether to auto-renew before expiry
    private boolean autoRenew = true;
}