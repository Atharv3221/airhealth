package github.atharv3221.airhealth.dto;

public record SubscriptionResponse(Long id, Long areaId, String areaName,
        double latitude, double longitude, int aqiThreshold, boolean active) {}
