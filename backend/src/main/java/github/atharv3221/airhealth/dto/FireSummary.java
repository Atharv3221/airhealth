package github.atharv3221.airhealth.dto;

import java.time.Instant;

// nearestKm, maxFrp and latest are null when count is 0
public record FireSummary(int count, Double nearestKm, Double maxFrp, Instant latest,
        int radiusKm, int days) {}
