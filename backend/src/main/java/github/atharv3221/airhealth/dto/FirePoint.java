package github.atharv3221.airhealth.dto;

import java.time.Instant;

public record FirePoint(double lat, double lon, double frp,
        String confidence, Instant detectedAt) {}
