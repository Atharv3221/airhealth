package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.client.GeocodingClient;
import github.atharv3221.airhealth.client.ReverseGeocodingClient;
import github.atharv3221.airhealth.dto.PlaceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaceService {

  static final int MAX_QUERY_LENGTH = 100;
  static final int RESULT_COUNT = 5;

  private final GeocodingClient geocodingClient;
  private final ReverseGeocodingClient reverseClient;
  // place names don't change, so reverse results are cached without expiry (LRU-bounded);
  // this also keeps us well under Nominatim's rate limit
  private final Map<String, PlaceResponse> reverseCache;

  public PlaceService(GeocodingClient geocodingClient,
                      ReverseGeocodingClient reverseClient,
                      @Value("${places.reverse-cache.max-entries}") int maxEntries) {
    this.geocodingClient = geocodingClient;
    this.reverseClient = reverseClient;
    this.reverseCache = new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, PlaceResponse> eldest) {
        return size() > maxEntries;
      }
    };
  }

  public List<PlaceResponse> search(String query) {
    String q = query == null ? "" : query.trim();
    if (q.isEmpty() || q.length() > MAX_QUERY_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must be 1-100 characters");
    }
    try {
      return geocodingClient.search(q, RESULT_COUNT);
    } catch (RestClientException e) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Place search unavailable", e);
    }
  }

  public PlaceResponse reverse(double lat, double lon) {
    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinates out of range");
    }
    // ~100 m buckets: nearby clicks share a lookup
    String key = String.format("%.3f,%.3f", lat, lon);

    PlaceResponse named;
    synchronized (reverseCache) {
      named = reverseCache.get(key);
    }
    if (named == null) {
      try {
        named = reverseClient.reverse(lat, lon);
      } catch (RestClientException e) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Place lookup unavailable", e);
      }
      synchronized (reverseCache) {
        reverseCache.put(key, named);
      }
    }
    // always answer with the exact point asked about, not the cached bucket's
    return new PlaceResponse(named.name(), lat, lon, named.admin1(), named.country());
  }
}
