package github.atharv3221.airhealth.service;

import java.time.Instant;
import java.util.Locale;

import github.atharv3221.airhealth.dto.FirePoint;
import github.atharv3221.airhealth.dto.FireSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FireServiceTest {

  private static final String HEADER =
      "latitude,longitude,bright_ti4,scan,track,acq_date,acq_time,satellite,instrument,"
          + "confidence,version,bright_ti5,frp,daynight";
  private static final String BASE =
      "https://firms.modaps.eosdis.nasa.gov/api/area/csv/KEY/VIIRS_SNPP_NRT/";

  private MockRestServiceServer server;
  private FireService fireService;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    fireService = new FireService("KEY", builder.build());
  }

  private static String csv(String... rows) {
    return HEADER + "\n" + String.join("\n", rows) + "\n";
  }

  private static String row(String conf, String time, String frp) {
    return rowAt("18.5", "73.8", conf, time, frp);
  }

  private static String rowAt(String lat, String lon, String conf, String time, String frp) {
    return lat + "," + lon + ",330.1,0.4,0.4,2026-10-10," + time + ",N,VIIRS," + conf
        + ",2.0NRT,290.2," + frp + ",D";
  }

  @Test
  void callsFirmsWithKeyRoundedBoxAndDays() {
    server.expect(requestTo(BASE + "73.1,18.0,74.0,19.0/3"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    fireService.getFires(73.12, 18.04, 73.96, 18.98, 3);
    server.verify();
  }

  @Test
  void roundsBoxOutwardSoEdgesAreNotClipped() {
    // west/south round down, east/north round up
    server.expect(requestTo(BASE + "73.1,18.1,73.9,18.9/2"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    fireService.getFires(73.16, 18.19, 73.81, 18.82, 2);
    server.verify();
  }

  @Test
  void roundingIgnoresFloatingPointNoise() {
    // 0.3 * 10 is 3.0000000000000004, which must not ceil to 0.4
    server.expect(requestTo(BASE + "-0.3,0.1,0.3,0.7/2"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    fireService.getFires(-0.3, 0.1, 0.3, 0.7, 2);
    server.verify();
  }

  @Test
  void usesDotDecimalsRegardlessOfDefaultLocale() {
    Locale previous = Locale.getDefault();
    Locale.setDefault(Locale.GERMANY);
    try {
      server.expect(requestTo(BASE + "73.5,18.5,74.5,19.5/2"))
          .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

      fireService.getFires(73.5, 18.5, 74.5, 19.5, 2);
      server.verify();
    } finally {
      Locale.setDefault(previous);
    }
  }

  @Test
  void parsesRowsIntoFirePoints() {
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(row("n", "0842", "5.3"), row("h", "1530", "12.7")),
            MediaType.TEXT_PLAIN));

    assertThat(fireService.getFires(73, 18, 74, 19, 2)).containsExactly(
        new FirePoint(18.5, 73.8, 5.3, "n", Instant.parse("2026-10-10T08:42:00Z")),
        new FirePoint(18.5, 73.8, 12.7, "h", Instant.parse("2026-10-10T15:30:00Z")));
  }

  @Test
  void padsShortAcquisitionTimes() {
    // FIRMS sends acq_time as an int, so 00:05 arrives as "5"
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(row("n", "5", "1.0")), MediaType.TEXT_PLAIN));

    assertThat(fireService.getFires(73, 18, 74, 19, 2))
        .extracting(FirePoint::detectedAt)
        .containsExactly(Instant.parse("2026-10-10T00:05:00Z"));
  }

  @Test
  void dropsLowConfidenceAndMalformedRows() {
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(row("l", "0842", "5.3"), "18.5,73.8,330.1",
            row("n", "0900", "2.0")), MediaType.TEXT_PLAIN));

    assertThat(fireService.getFires(73, 18, 74, 19, 2))
        .extracting(FirePoint::confidence)
        .containsExactly("n");
  }

  @Test
  void returnsEmptyListForHeaderOnlyOrEmptyBody() {
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/3"))
        .andRespond(withSuccess());

    assertThat(fireService.getFires(73, 18, 74, 19, 2)).isEmpty();
    assertThat(fireService.getFires(73, 18, 74, 19, 3)).isEmpty();
  }

  @Test
  void nearbyViewsShareCacheEntry() {
    server.expect(ExpectedCount.once(), requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(row("n", "0842", "5.3")), MediaType.TEXT_PLAIN));

    var first = fireService.getFires(73.01, 18.02, 73.96, 18.97, 2);
    // rounds outward to the same 1-decimal box
    var second = fireService.getFires(73.04, 18.07, 73.92, 18.91, 2);

    assertThat(second).isEqualTo(first);
    server.verify();
  }

  @Test
  void differentDaysAreFetchedSeparately() {
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/1"))
        .andRespond(withSuccess(csv(row("n", "0842", "5.3")), MediaType.TEXT_PLAIN));
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/5"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    assertThat(fireService.getFires(73, 18, 74, 19, 1)).hasSize(1);
    assertThat(fireService.getFires(73, 18, 74, 19, 5)).isEmpty();
    server.verify();
  }

  @Test
  void doesNotCacheWhenUpstreamFails() {
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2")).andRespond(withServerError());
    server.expect(requestTo(BASE + "73.0,18.0,74.0,19.0/2"))
        .andRespond(withSuccess(csv(row("n", "0842", "5.3")), MediaType.TEXT_PLAIN));

    assertThatThrownBy(() -> fireService.getFires(73, 18, 74, 19, 2))
        .isInstanceOf(HttpServerErrorException.class);
    assertThat(fireService.getFires(73, 18, 74, 19, 2)).hasSize(1);
    server.verify();
  }

  @Test
  void summarizeQueriesBoxAroundPoint() {
    // 50 km is ~0.45° of latitude and ~0.48° of longitude at 18.5°N, rounded outward
    server.expect(requestTo(BASE + "73.3,18.0,74.3,19.0/2"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    fireService.summarize(18.5, 73.8, 50, 2);
    server.verify();
  }

  @Test
  void summarizeCountsOnlyFiresInsideRadius() {
    server.expect(requestTo(BASE + "73.3,18.0,74.3,19.0/2"))
        .andRespond(withSuccess(csv(
            rowAt("18.6", "73.8", "n", "0842", "5.3"),   // ~11 km
            rowAt("18.9", "73.8", "h", "1530", "12.7"),  // ~44 km
            rowAt("18.9", "74.2", "h", "1700", "40.0")), // box corner, ~61 km
            MediaType.TEXT_PLAIN));

    assertThat(fireService.summarize(18.5, 73.8, 50, 2)).isEqualTo(new FireSummary(
        2, 11.1, 12.7, Instant.parse("2026-10-10T15:30:00Z"), 50, 2));
  }

  @Test
  void summarizeReturnsNullDetailsWhenNoFires() {
    server.expect(requestTo(BASE + "73.3,18.0,74.3,19.0/3"))
        .andRespond(withSuccess(csv(rowAt("18.9", "74.2", "n", "0842", "5.3")),
            MediaType.TEXT_PLAIN));

    assertThat(fireService.summarize(18.5, 73.8, 50, 3))
        .isEqualTo(new FireSummary(0, null, null, null, 50, 3));
  }

  @Test
  void summarizeClampsBoxAtAntimeridian() {
    server.expect(requestTo(BASE + "179.3,-0.5,180.0,0.5/2"))
        .andRespond(withSuccess(csv(), MediaType.TEXT_PLAIN));

    fireService.summarize(0, 179.8, 50, 2);
    server.verify();
  }

  @Test
  void summarizeSharesCacheWithBoxLookups() {
    server.expect(ExpectedCount.once(), requestTo(BASE + "73.3,18.0,74.3,19.0/2"))
        .andRespond(withSuccess(csv(rowAt("18.6", "73.8", "n", "0842", "5.3")),
            MediaType.TEXT_PLAIN));

    fireService.summarize(18.5, 73.8, 50, 2);
    assertThat(fireService.summarize(18.5, 73.8, 50, 2).count()).isEqualTo(1);
    server.verify();
  }
}
