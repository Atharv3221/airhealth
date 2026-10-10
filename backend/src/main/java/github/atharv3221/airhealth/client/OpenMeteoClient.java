package github.atharv3221.airhealth.client;

import github.atharv3221.airhealth.dto.AirQualityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

  private static final String CURRENT_FIELDS =
      "us_aqi,pm10,pm2_5,nitrogen_dioxide,sulphur_dioxide,ozone,carbon_monoxide";

  private final RestClient restClient;

  public OpenMeteoClient(@Value("${openmeteo.air-url}") String baseUrl) {
    this.restClient = RestClient.create(baseUrl);
  }

  public AirQualityResponse getCurrent(double lat, double lon) {
    return restClient.get()
        .uri(uri -> uri.path("/v1/air-quality")
            .queryParam("latitude", lat)
            .queryParam("longitude", lon)
            .queryParam("current", CURRENT_FIELDS)
            .build())
        .retrieve()
        .body(AirQualityResponse.class);
  }
}
