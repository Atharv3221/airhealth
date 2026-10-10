package github.atharv3221.airhealth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AirQualityResponse(Current current) {

  public record Current(
      String time,
      @JsonProperty("us_aqi") Integer usAqi,
      Double pm10,
      @JsonProperty("pm2_5") Double pm25,
      @JsonProperty("nitrogen_dioxide") Double no2,
      @JsonProperty("sulphur_dioxide") Double so2,
      Double ozone,
      @JsonProperty("carbon_monoxide") Double co) {
  }
}
