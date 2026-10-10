package github.atharv3221.airhealth.dto;

public record CreateSubscriptionRequest(String areaName, double latitude,
       double longitude, Integer aqiThreshold) {}
