package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.JwtService;
import github.atharv3221.airhealth.dto.AirQualityResponse;
import github.atharv3221.airhealth.service.AirService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class AirControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private AirService airService;

  private String token() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 7L);
    return jwtService.generateToken(user);
  }

  @Test
  void getReturnsCurrentAirQuality() throws Exception {
    when(airService.getCurrent(12.97, 77.59)).thenReturn(new AirQualityResponse(
        new AirQualityResponse.Current("2026-10-10T12:00", 85, 40.0, 25.5, 12.0, 3.0, 60.0, 300.0)));

    mockMvc.perform(get("/api/air").param("lat", "12.97").param("lon", "77.59")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.current.time").value("2026-10-10T12:00"))
        .andExpect(jsonPath("$.current.us_aqi").value(85))
        .andExpect(jsonPath("$.current.pm2_5").value(25.5));
  }

  @Test
  void getReturns400WhenCoordinatesMissing() throws Exception {
    mockMvc.perform(get("/api/air").param("lat", "12.97")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadRequest());
    verify(airService, never()).getCurrent(anyDouble(), anyDouble());
  }

  @Test
  void getReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/air").param("lat", "12.97").param("lon", "77.59"))
        .andExpect(status().isUnauthorized());
  }
}
