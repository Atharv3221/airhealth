package github.atharv3221.airhealth.controllers;

import java.time.Instant;
import java.util.List;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.dto.FirePoint;
import github.atharv3221.airhealth.dto.FireSummary;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.FireService;
import github.atharv3221.airhealth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FireController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class FireControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private FireService fireService;

  private String token() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 7L);
    return jwtService.generateToken(user);
  }

  private MockHttpServletRequestBuilder fires(String west, String south, String east,
                                              String north) {
    return get("/api/fires")
        .param("west", west).param("south", south)
        .param("east", east).param("north", north)
        .header("Authorization", "Bearer " + token());
  }

  @Test
  void getReturnsFiresWithDefaultDays() throws Exception {
    when(fireService.getFires(73.0, 18.0, 74.0, 19.0, 2)).thenReturn(List.of(
        new FirePoint(18.5, 73.8, 5.3, "n", Instant.parse("2026-10-10T08:42:00Z"))));

    mockMvc.perform(fires("73", "18", "74", "19"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].lat").value(18.5))
        .andExpect(jsonPath("$[0].lon").value(73.8))
        .andExpect(jsonPath("$[0].frp").value(5.3))
        .andExpect(jsonPath("$[0].confidence").value("n"))
        .andExpect(jsonPath("$[0].detectedAt").value("2026-10-10T08:42:00Z"));
  }

  @Test
  void getPassesDaysThrough() throws Exception {
    when(fireService.getFires(73.0, 18.0, 74.0, 19.0, 4)).thenReturn(List.of());

    mockMvc.perform(fires("73", "18", "74", "19").param("days", "4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    verify(fireService).getFires(73.0, 18.0, 74.0, 19.0, 4);
  }

  @Test
  void getClampsDaysToOneThroughFive() throws Exception {
    mockMvc.perform(fires("73", "18", "74", "19").param("days", "0"))
        .andExpect(status().isOk());
    mockMvc.perform(fires("73", "18", "74", "19").param("days", "30"))
        .andExpect(status().isOk());

    verify(fireService).getFires(73.0, 18.0, 74.0, 19.0, 1);
    verify(fireService).getFires(73.0, 18.0, 74.0, 19.0, 5);
  }

  @Test
  void getAllowsBoxOfExactlyFifteenDegrees() throws Exception {
    mockMvc.perform(fires("70", "10", "85", "25"))
        .andExpect(status().isOk());
    verify(fireService).getFires(70.0, 10.0, 85.0, 25.0, 2);
  }

  @Test
  void getReturns400WhenBoxTooWide() throws Exception {
    mockMvc.perform(fires("70", "18", "85.5", "19"))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).getFires(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt());
  }

  @Test
  void getReturns400WhenBoxTooTall() throws Exception {
    mockMvc.perform(fires("73", "10", "74", "25.5"))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).getFires(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt());
  }

  @Test
  void getReturns400WhenEastIsWestOfWest() throws Exception {
    // a negative width would otherwise slip past the size check
    mockMvc.perform(fires("74", "18", "73", "19"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(fires("179", "18", "-179", "19"))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).getFires(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt());
  }

  @Test
  void getReturns400WhenNorthIsSouthOfSouth() throws Exception {
    mockMvc.perform(fires("73", "19", "74", "18"))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).getFires(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt());
  }

  @Test
  void getReturns400WhenBoundsMissing() throws Exception {
    mockMvc.perform(get("/api/fires").param("west", "73").param("south", "18")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).getFires(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyInt());
  }

  @Test
  void getReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/fires").param("west", "73").param("south", "18")
            .param("east", "74").param("north", "19"))
        .andExpect(status().isUnauthorized());
  }

  private MockHttpServletRequestBuilder nearby(String lat, String lon) {
    return get("/api/fires/nearby").param("lat", lat).param("lon", lon)
        .header("Authorization", "Bearer " + token());
  }

  @Test
  void nearbyReturnsSummaryWithDefaults() throws Exception {
    when(fireService.summarize(18.5, 73.8, 50, 2)).thenReturn(new FireSummary(
        3, 11.1, 12.7, Instant.parse("2026-10-10T15:30:00Z"), 50, 2));

    mockMvc.perform(nearby("18.5", "73.8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(3))
        .andExpect(jsonPath("$.nearestKm").value(11.1))
        .andExpect(jsonPath("$.maxFrp").value(12.7))
        .andExpect(jsonPath("$.latest").value("2026-10-10T15:30:00Z"))
        .andExpect(jsonPath("$.radiusKm").value(50))
        .andExpect(jsonPath("$.days").value(2));
  }

  @Test
  void nearbyReturnsNullDetailsWhenNoFires() throws Exception {
    when(fireService.summarize(18.5, 73.8, 50, 2))
        .thenReturn(new FireSummary(0, null, null, null, 50, 2));

    mockMvc.perform(nearby("18.5", "73.8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(0))
        .andExpect(jsonPath("$.nearestKm").doesNotExist())
        .andExpect(jsonPath("$.latest").doesNotExist());
  }

  @Test
  void nearbyClampsRadiusAndDays() throws Exception {
    mockMvc.perform(nearby("18.5", "73.8").param("radiusKm", "5").param("days", "0"))
        .andExpect(status().isOk());
    mockMvc.perform(nearby("18.5", "73.8").param("radiusKm", "500").param("days", "9"))
        .andExpect(status().isOk());

    verify(fireService).summarize(18.5, 73.8, 10, 1);
    verify(fireService).summarize(18.5, 73.8, 100, 5);
  }

  @Test
  void nearbyReturns400ForInvalidLocation() throws Exception {
    mockMvc.perform(nearby("86", "73.8")).andExpect(status().isBadRequest());
    mockMvc.perform(nearby("18.5", "-181")).andExpect(status().isBadRequest());
    mockMvc.perform(nearby("NaN", "73.8")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/fires/nearby").param("lat", "18.5")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadRequest());
    verify(fireService, never()).summarize(anyDouble(), anyDouble(), anyInt(), anyInt());
  }

  @Test
  void nearbyReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/fires/nearby").param("lat", "18.5").param("lon", "73.8"))
        .andExpect(status().isUnauthorized());
  }
}
