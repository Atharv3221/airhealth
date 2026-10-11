package github.atharv3221.airhealth.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import github.atharv3221.airhealth.dto.FirePoint;
import github.atharv3221.airhealth.dto.FireSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FireService {
  private static final Duration TTL = Duration.ofMinutes(30);
  private record Cached(Instant at, List<FirePoint> data) {}

  private final Map<String, Cached> cache = new ConcurrentHashMap<>();
  private final String key;
  private final RestClient client;

  @Autowired
  public FireService(@Value("${firms.key}") String key) {
    this(key, RestClient.create());
  }

  FireService(String key, RestClient client) {
    this.key = key;
    this.client = client;
  }

  public List<FirePoint> getFires(double west, double south, double east, double north, int days) {
    // round outward so nearby map views share one cache entry without clipping the edges
    String box = String.format(Locale.ROOT, "%.1f,%.1f,%.1f,%.1f",
        floor1(west), floor1(south), ceil1(east), ceil1(north));
    String cacheKey = box + "/" + days;

    Cached c = cache.get(cacheKey);
    if (c != null && c.at().plus(TTL).isAfter(Instant.now())) return c.data();

    String url = "https://firms.modaps.eosdis.nasa.gov/api/area/csv/%s/VIIRS_SNPP_NRT/%s/%d"
        .formatted(key, box, days);
    String csv = client.get().uri(url).retrieve().body(String.class);

    List<FirePoint> fires = parse(csv);
    cache.put(cacheKey, new Cached(Instant.now(), fires));
    return fires;
  }

  /** Fires within radiusKm of a point, from the same cached box lookup as getFires. */
  public FireSummary summarize(double lat, double lon, int radiusKm, int days) {
    double dLat = radiusKm / 111.0;
    double dLon = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
    List<FirePoint> all = getFires(Math.max(lon - dLon, -180), Math.max(lat - dLat, -90),
        Math.min(lon + dLon, 180), Math.min(lat + dLat, 90), days);

    int count = 0;
    double nearest = Double.MAX_VALUE, maxFrp = 0;
    Instant latest = null;
    for (FirePoint f : all) {
      // the box's corners reach past the circle
      double d = haversineKm(lat, lon, f.lat(), f.lon());
      if (d > radiusKm) continue;
      count++;
      nearest = Math.min(nearest, d);
      maxFrp = Math.max(maxFrp, f.frp());
      if (latest == null || f.detectedAt().isAfter(latest)) latest = f.detectedAt();
    }
    return count == 0
        ? new FireSummary(0, null, null, null, radiusKm, days)
        : new FireSummary(count, Math.round(nearest * 10) / 10.0, maxFrp, latest, radiusKm, days);
  }

  private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return 6371 * 2 * Math.asin(Math.sqrt(a));
  }

  // epsilon absorbs float noise like 0.3 * 10 = 3.0000000000000004
  private static double floor1(double v) { return Math.floor(v * 10 + 1e-9) / 10; }
  private static double ceil1(double v) { return Math.ceil(v * 10 - 1e-9) / 10; }

  private List<FirePoint> parse(String csv) {
    List<FirePoint> out = new ArrayList<>();
    if (csv == null) return out;
    String[] lines = csv.split("\\R");
    for (int i = 1; i < lines.length; i++) {          // skip header
      String[] f = lines[i].split(",");
      if (f.length < 14) continue;
      String conf = f[9];
      if (conf.equals("l")) continue;                // drop low confidence
      String hhmm = "%04d".formatted(Integer.parseInt(f[6]));
      Instant t = LocalDateTime.of(LocalDate.parse(f[5]),
              LocalTime.of(Integer.parseInt(hhmm.substring(0, 2)),
                  Integer.parseInt(hhmm.substring(2))))
          .toInstant(ZoneOffset.UTC);
      out.add(new FirePoint(Double.parseDouble(f[0]), Double.parseDouble(f[1]),
          Double.parseDouble(f[12]), conf, t));
    }
    return out;
  }
}
