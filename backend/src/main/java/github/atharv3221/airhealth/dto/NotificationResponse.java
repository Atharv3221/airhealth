package github.atharv3221.airhealth.dto;

import java.time.Instant;

public record NotificationResponse(Long id, Long subscriptionId, String message,
        boolean read, Instant createdAt) {}
