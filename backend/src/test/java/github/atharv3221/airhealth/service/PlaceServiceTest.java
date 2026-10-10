package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.client.GeocodingClient;
import github.atharv3221.airhealth.client.ReverseGeocodingClient;
import github.atharv3221.airhealth.dto.PlaceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

  @Mock
  private GeocodingClient geocodingClient;

  @Mock
  private ReverseGeocodingClient reverseClient;

  private PlaceService placeService;

  @BeforeEach
  void setUp() {
    placeService = new PlaceService(geocodingClient, reverseClient, 1000);
  }

  private static void assertStatus(Runnable call, HttpStatus status) {
    assertThatThrownBy(call::run).isInstanceOfSatisfying(ResponseStatusException.class,
        e -> assertThat(e.getStatusCode()).isEqualTo(status));
  }

  @Test
  void searchTrimsQueryAndReturnsResults() {
    List<PlaceResponse> places = List.of(new PlaceResponse("Pune", 18.52, 73.86, "Maharashtra", "India"));
    when(geocodingClient.search("Pune", 5)).thenReturn(places);

    assertThat(placeService.search("  Pune ")).isEqualTo(places);
  }

  @Test
  void searchRejectsBlankOrTooLongQuery() {
    assertStatus(() -> placeService.search("   "), HttpStatus.BAD_REQUEST);
    assertStatus(() -> placeService.search(null), HttpStatus.BAD_REQUEST);
    assertStatus(() -> placeService.search("x".repeat(101)), HttpStatus.BAD_REQUEST);
    verify(geocodingClient, never()).search(anyString(), anyInt());
  }

  @Test
  void searchMapsUpstreamFailureToBadGateway() {
    when(geocodingClient.search("Pune", 5)).thenThrow(new ResourceAccessException("timeout"));

    assertStatus(() -> placeService.search("Pune"), HttpStatus.BAD_GATEWAY);
  }

  @Test
  void reverseReturnsNameWithRequestedCoordinates() {
    when(reverseClient.reverse(18.5308, 73.8475))
        .thenReturn(new PlaceResponse("Shivajinagar", 18.5308, 73.8475, "Maharashtra", "India"));

    assertThat(placeService.reverse(18.5308, 73.8475)).isEqualTo(
        new PlaceResponse("Shivajinagar", 18.5308, 73.8475, "Maharashtra", "India"));
  }

  @Test
  void reverseCachesNearbyPointsButKeepsEachPointsCoordinates() {
    when(reverseClient.reverse(18.5308, 73.8475))
        .thenReturn(new PlaceResponse("Shivajinagar", 18.5308, 73.8475, "Maharashtra", "India"));

    placeService.reverse(18.5308, 73.8475);
    // same ~100 m bucket "18.531,73.848"
    PlaceResponse second = placeService.reverse(18.5311, 73.8478);

    assertThat(second).isEqualTo(new PlaceResponse("Shivajinagar", 18.5311, 73.8478, "Maharashtra", "India"));
    verify(reverseClient, times(1)).reverse(anyDouble(), anyDouble());
  }

  @Test
  void reverseRejectsOutOfRangeCoordinates() {
    assertStatus(() -> placeService.reverse(91, 0), HttpStatus.BAD_REQUEST);
    assertStatus(() -> placeService.reverse(0, -181), HttpStatus.BAD_REQUEST);
    verify(reverseClient, never()).reverse(anyDouble(), anyDouble());
  }

  @Test
  void reverseMapsUpstreamFailureToBadGatewayAndDoesNotCache() {
    when(reverseClient.reverse(18.53, 73.85))
        .thenThrow(new ResourceAccessException("down"))
        .thenReturn(new PlaceResponse("Shivajinagar", 18.53, 73.85, "Maharashtra", "India"));

    assertStatus(() -> placeService.reverse(18.53, 73.85), HttpStatus.BAD_GATEWAY);
    assertThat(placeService.reverse(18.53, 73.85).name()).isEqualTo("Shivajinagar");
  }
}
