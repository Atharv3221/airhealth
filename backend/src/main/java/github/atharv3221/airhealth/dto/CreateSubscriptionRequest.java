package github.atharv3221.airhealth.dto;

import github.atharv3221.airhealth.entity.SubscriptionType;

// type is optional and defaults to OTHER
public record CreateSubscriptionRequest(String areaName, double latitude,
       double longitude, Integer aqiThreshold, SubscriptionType type) {}
