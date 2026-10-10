package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.client.OpenMeteoClient;
import github.atharv3221.airhealth.dto.AirQualityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AirService {

  private record Entry(AirQualityResponse value, Instant expiresAt) {
  }

  private final OpenMeteoClient client;
  private final long ttlSeconds;
  private final Map<String, Entry> cache;

  public AirService(OpenMeteoClient client,
                    @Value("${air.cache.ttl-seconds}") long ttlSeconds,
                    @Value("${air.cache.max-entries}") int maxEntries) {
    this.client = client;
    this.ttlSeconds = ttlSeconds;
    this.cache = new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
        return size() > maxEntries;
      }
    };
  }

  public AirQualityResponse getCurrent(double lat, double lon) {
    String key = String.format("%.2f,%.2f", lat, lon);

    synchronized (cache) {
      Entry hit = cache.get(key);
      if (hit != null && hit.expiresAt().isAfter(Instant.now())) {
        return hit.value();
      }
    }

    AirQualityResponse fresh = client.getCurrent(lat, lon);

    synchronized (cache) {
      cache.put(key, new Entry(fresh, Instant.now().plusSeconds(ttlSeconds)));
    }
    return fresh;
  }
}
