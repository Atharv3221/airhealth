package github.atharv3221.airhealth.dto;

import github.atharv3221.airhealth.entity.SubscriptionType;

// either field may be left out (null) to keep its current value
public record UpdateSubscriptionRequest(String name, SubscriptionType type) {}
