package github.atharv3221.airhealth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import github.atharv3221.airhealth.dto.PlaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.stream.Stream;

// OpenStreetMap Nominatim: coordinates -> area name (Open-Meteo has no reverse lookup).
// Usage policy: identify the app via User-Agent and stay under 1 request/second.
@Component
public class ReverseGeocodingClient {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record ReverseResponse(String name, Address address) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Address(String neighbourhood, String suburb,
                 @JsonProperty("city_district") String cityDistrict,
                 String village, String town, String city, String county,
                 String state, String country) {
  }

  private final RestClient restClient;

  @Autowired
  public ReverseGeocodingClient(@Value("${nominatim.url}") String baseUrl,
                                @Value("${nominatim.user-agent}") String userAgent) {
    this(RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
        .build());
  }

  ReverseGeocodingClient(RestClient restClient) {
    this.restClient = restClient;
  }

  /** The place at a point, keeping the given coordinates; name is null when nothing is there (e.g. sea). */
  public PlaceResponse reverse(double lat, double lon) {
    ReverseResponse response = restClient.get()
        .uri(uri -> uri.path("/reverse")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .queryParam("format", "jsonv2")
            .queryParam("zoom", 14) // suburb level
            .queryParam("accept-language", "en")
            .build())
        .retrieve()
        .body(ReverseResponse.class);

    Address a = response == null ? null : response.address();
    if (a == null) {
      return new PlaceResponse(null, lat, lon, null, null);
    }
    // most specific area-like name first
    String name = Stream.of(a.suburb(), a.neighbourhood(), a.cityDistrict(), a.village(),
            a.town(), a.city(), a.county(), response.name())
        .filter(s -> s != null && !s.isBlank())
        .findFirst()
        .orElse(null);
    return new PlaceResponse(name, lat, lon, a.state(), a.country());
  }
}
