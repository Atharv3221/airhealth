package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.client.OpenMeteoClient;
import github.atharv3221.airhealth.dto.AirQualityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirServiceTest {

  @Mock
  private OpenMeteoClient client;

  private static AirQualityResponse air(int aqi) {
    return new AirQualityResponse(new AirQualityResponse.Current(
        "2026-10-10T12:00", aqi, 20.0, 10.0, 5.0, 1.0, 30.0, 200.0));
  }

  @Test
  void fetchesFromClientOnCacheMiss() {
    AirService airService = new AirService(client, 1800, 500);
    when(client.getCurrent(12.97, 77.59)).thenReturn(air(80));

    assertThat(airService.getCurrent(12.97, 77.59)).isEqualTo(air(80));
    verify(client).getCurrent(12.97, 77.59);
  }

  @Test
  void returnsCachedValueWithinTtl() {
    AirService airService = new AirService(client, 1800, 500);
    when(client.getCurrent(12.97, 77.59)).thenReturn(air(80));

    airService.getCurrent(12.97, 77.59);
    AirQualityResponse second = airService.getCurrent(12.97, 77.59);

    assertThat(second).isEqualTo(air(80));
    verify(client, times(1)).getCurrent(anyDouble(), anyDouble());
  }

  @Test
  void nearbyCoordinatesShareCacheEntry() {
    AirService airService = new AirService(client, 1800, 500);
    when(client.getCurrent(12.971, 77.591)).thenReturn(air(80));

    airService.getCurrent(12.971, 77.591);
    // rounds to the same 2-decimal key "12.97,77.59"
    assertThat(airService.getCurrent(12.974, 77.589)).isEqualTo(air(80));

    verify(client, times(1)).getCurrent(anyDouble(), anyDouble());
  }

  @Test
  void differentCoordinatesAreFetchedSeparately() {
    AirService airService = new AirService(client, 1800, 500);
    when(client.getCurrent(12.97, 77.59)).thenReturn(air(80));
    when(client.getCurrent(28.61, 77.21)).thenReturn(air(180));

    assertThat(airService.getCurrent(12.97, 77.59)).isEqualTo(air(80));
    assertThat(airService.getCurrent(28.61, 77.21)).isEqualTo(air(180));
  }

  @Test
  void refetchesAfterTtlExpires() {
    AirService airService = new AirService(client, 0, 500);
    when(client.getCurrent(12.97, 77.59)).thenReturn(air(80), air(95));

    assertThat(airService.getCurrent(12.97, 77.59)).isEqualTo(air(80));
    assertThat(airService.getCurrent(12.97, 77.59)).isEqualTo(air(95));
    verify(client, times(2)).getCurrent(12.97, 77.59);
  }

  @Test
  void evictsLeastRecentlyUsedEntryWhenFull() {
    AirService airService = new AirService(client, 1800, 2);
    when(client.getCurrent(anyDouble(), anyDouble())).thenReturn(air(50));

    airService.getCurrent(1.0, 1.0);
    airService.getCurrent(2.0, 2.0);
    airService.getCurrent(1.0, 1.0); // touch 1 so 2 becomes eldest
    airService.getCurrent(3.0, 3.0); // evicts 2

    airService.getCurrent(1.0, 1.0); // still cached
    airService.getCurrent(2.0, 2.0); // fetched again

    verify(client, times(1)).getCurrent(1.0, 1.0);
    verify(client, times(2)).getCurrent(2.0, 2.0);
    verify(client, times(1)).getCurrent(3.0, 3.0);
  }

  @Test
  void doesNotCacheWhenClientFails() {
    AirService airService = new AirService(client, 1800, 500);
    when(client.getCurrent(12.97, 77.59))
        .thenThrow(new RuntimeException("down"))
        .thenReturn(air(80));

    assertThatThrownBy(() -> airService.getCurrent(12.97, 77.59)).hasMessage("down");
    assertThat(airService.getCurrent(12.97, 77.59)).isEqualTo(air(80));
  }
}
