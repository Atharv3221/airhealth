package github.atharv3221.airhealth.dto;

import github.atharv3221.airhealth.entity.SubscriptionType;

// name is the user's label (falls back to areaName); areaName is the shared area's name
public record SubscriptionResponse(Long id, String name, Long areaId, String areaName,
        double latitude, double longitude, int aqiThreshold, boolean active,
        SubscriptionType type) {}
