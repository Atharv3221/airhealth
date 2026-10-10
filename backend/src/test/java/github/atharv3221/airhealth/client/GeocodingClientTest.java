package github.atharv3221.airhealth.client;

import github.atharv3221.airhealth.dto.PlaceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingClientTest {

  private MockRestServiceServer server;
  private GeocodingClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl("https://geo.test");
    server = MockRestServiceServer.bindTo(builder).build();
    client = new GeocodingClient(builder.build());
  }

  @Test
  void searchMapsResultsAndIgnoresExtraFields() {
    server.expect(requestTo(startsWith("https://geo.test/v1/search")))
        .andExpect(queryParam("name", "Pune"))
        .andExpect(queryParam("count", "5"))
        .andRespond(withSuccess("""
            {"results":[{"id":1259229,"name":"Pune","latitude":18.51957,"longitude":73.85535,
              "elevation":560.0,"country_code":"IN","admin1":"Maharashtra","country":"India",
              "timezone":"Asia/Kolkata","population":2935744}],
             "generationtime_ms":0.5}
            """, MediaType.APPLICATION_JSON));

    assertThat(client.search("Pune", 5)).containsExactly(
        new PlaceResponse("Pune", 18.51957, 73.85535, "Maharashtra", "India"));
    server.verify();
  }

  @Test
  void searchReturnsEmptyListWhenNoResultsKey() {
    server.expect(requestTo(startsWith("https://geo.test/v1/search")))
        .andRespond(withSuccess("{\"generationtime_ms\":0.2}", MediaType.APPLICATION_JSON));

    assertThat(client.search("zzzzqqq", 5)).isEmpty();
  }

  @Test
  void searchPropagatesUpstreamErrors() {
    server.expect(requestTo(startsWith("https://geo.test/v1/search")))
        .andRespond(withServerError());

    assertThatThrownBy(() -> client.search("Pune", 5)).isInstanceOf(HttpServerErrorException.class);
  }
}
