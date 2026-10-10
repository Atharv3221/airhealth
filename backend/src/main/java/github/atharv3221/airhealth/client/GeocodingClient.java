package github.atharv3221.airhealth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import github.atharv3221.airhealth.dto.PlaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

// Open-Meteo geocoding: place name -> coordinates
@Component
public class GeocodingClient {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record SearchResponse(List<Result> results) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Result(String name, double latitude, double longitude, String admin1, String country) {
  }

  private final RestClient restClient;

  @Autowired
  public GeocodingClient(@Value("${openmeteo.geocoding-url}") String baseUrl) {
    this(RestClient.create(baseUrl));
  }

  GeocodingClient(RestClient restClient) {
    this.restClient = restClient;
  }

  public List<PlaceResponse> search(String name, int count) {
    SearchResponse response = restClient.get()
        .uri(uri -> uri.path("/v1/search")
            .queryParam("name", name)
            .queryParam("count", count)
            .queryParam("language", "en")
            .queryParam("format", "json")
            .build())
        .retrieve()
        .body(SearchResponse.class);

    // no matches -> Open-Meteo omits "results" entirely
    if (response == null || response.results() == null) {
      return List.of();
    }
    return response.results().stream()
        .map(r -> new PlaceResponse(r.name(), r.latitude(), r.longitude(), r.admin1(), r.country()))
        .toList();
  }
}
