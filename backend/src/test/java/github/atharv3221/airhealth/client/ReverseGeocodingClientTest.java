package github.atharv3221.airhealth.client;

import github.atharv3221.airhealth.dto.PlaceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ReverseGeocodingClientTest {

  private MockRestServiceServer server;
  private ReverseGeocodingClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder()
        .baseUrl("https://osm.test")
        .defaultHeader("User-Agent", "AirHealth-test");
    server = MockRestServiceServer.bindTo(builder).build();
    client = new ReverseGeocodingClient(builder.build());
  }

  @Test
  void reversePrefersSuburbAndKeepsRequestedCoordinates() {
    server.expect(requestTo(startsWith("https://osm.test/reverse")))
        .andExpect(queryParam("lat", "18.5308"))
        .andExpect(queryParam("format", "jsonv2"))
        .andExpect(header("User-Agent", "AirHealth-test"))
        .andRespond(withSuccess("""
            {"place_id":1,"lat":"18.531","lon":"73.847","name":"Some Road",
             "display_name":"Some Road, Shivajinagar, Pune, Maharashtra, India",
             "address":{"road":"Some Road","suburb":"Shivajinagar","city":"Pune",
               "state":"Maharashtra","country":"India","country_code":"in"}}
            """, MediaType.APPLICATION_JSON));

    assertThat(client.reverse(18.5308, 73.8475)).isEqualTo(
        new PlaceResponse("Shivajinagar", 18.5308, 73.8475, "Maharashtra", "India"));
    server.verify();
  }

  @Test
  void reverseFallsBackToCityWhenNoSuburb() {
    server.expect(requestTo(startsWith("https://osm.test/reverse")))
        .andRespond(withSuccess("""
            {"name":"","address":{"city":"Nashik","state":"Maharashtra","country":"India"}}
            """, MediaType.APPLICATION_JSON));

    assertThat(client.reverse(20.0, 73.79).name()).isEqualTo("Nashik");
  }

  @Test
  void reverseReturnsNullNameWhenNothingThere() {
    server.expect(requestTo(startsWith("https://osm.test/reverse")))
        .andRespond(withSuccess("{\"error\":\"Unable to geocode\"}", MediaType.APPLICATION_JSON));

    assertThat(client.reverse(0.0, -30.0)).isEqualTo(new PlaceResponse(null, 0.0, -30.0, null, null));
  }
}
