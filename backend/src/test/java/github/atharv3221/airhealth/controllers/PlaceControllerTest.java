package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.dto.PlaceResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.JwtService;
import github.atharv3221.airhealth.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class PlaceControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private PlaceService placeService;

  private String token() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 7L);
    return jwtService.generateToken(user);
  }

  @Test
  void searchReturnsPlaces() throws Exception {
    when(placeService.search("Pune")).thenReturn(List.of(
        new PlaceResponse("Pune", 18.52, 73.86, "Maharashtra", "India")));

    mockMvc.perform(get("/api/places/search").param("q", "Pune")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Pune"))
        .andExpect(jsonPath("$[0].latitude").value(18.52))
        .andExpect(jsonPath("$[0].admin1").value("Maharashtra"))
        .andExpect(jsonPath("$[0].country").value("India"));
  }

  @Test
  void searchReturns400WithoutQuery() throws Exception {
    mockMvc.perform(get("/api/places/search").header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void searchReturns502WhenUpstreamDown() throws Exception {
    when(placeService.search(anyString())).thenThrow(
        new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Place search unavailable"));

    mockMvc.perform(get("/api/places/search").param("q", "Pune")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadGateway());
  }

  @Test
  void searchReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/places/search").param("q", "Pune"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void reverseReturnsPlace() throws Exception {
    when(placeService.reverse(18.53, 73.85)).thenReturn(
        new PlaceResponse("Shivajinagar", 18.53, 73.85, "Maharashtra", "India"));

    mockMvc.perform(get("/api/places/reverse").param("lat", "18.53").param("lon", "73.85")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Shivajinagar"))
        .andExpect(jsonPath("$.longitude").value(73.85));
  }

  @Test
  void reverseReturns400ForNonNumericCoordinates() throws Exception {
    mockMvc.perform(get("/api/places/reverse").param("lat", "abc").param("lon", "73.85")
            .header("Authorization", "Bearer " + token()))
        .andExpect(status().isBadRequest());
  }
}
