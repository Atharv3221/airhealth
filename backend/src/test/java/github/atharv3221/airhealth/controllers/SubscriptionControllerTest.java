package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.JwtService;
import github.atharv3221.airhealth.dto.CreateSubscriptionRequest;
import github.atharv3221.airhealth.dto.SubscriptionResponse;
import github.atharv3221.airhealth.dto.UpdateSubscriptionRequest;
import github.atharv3221.airhealth.entity.SubscriptionType;
import github.atharv3221.airhealth.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class SubscriptionControllerTest {

  private static final String BODY =
      "{\"areaName\":\"Koramangala\",\"latitude\":12.93,\"longitude\":77.62,\"aqiThreshold\":150}";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private SubscriptionService subscriptionService;

  private String tokenFor(long id) {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", id);
    return jwtService.generateToken(user);
  }

  @Test
  void subscribeAreaUsesUserIdFromToken() throws Exception {
    when(subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, 150, null)))
        .thenReturn(new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true, SubscriptionType.OTHER));

    mockMvc.perform(post("/api/subscriptions/area")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.areaId").value(3))
        .andExpect(jsonPath("$.areaName").value("Koramangala"))
        .andExpect(jsonPath("$.aqiThreshold").value(150))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  void subscribeAreaReturns409WhenAlreadySubscribed() throws Exception {
    when(subscriptionService.createAreaSubscription(eq(7L), any())).thenThrow(
        new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed to this area"));

    mockMvc.perform(post("/api/subscriptions/area")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isConflict());
  }

  @Test
  void subscribeAreaReturns401WithoutToken() throws Exception {
    mockMvc.perform(post("/api/subscriptions/area")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void listMineReturnsSubscriptions() throws Exception {
    when(subscriptionService.listMine(7L)).thenReturn(List.of(
        new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true, SubscriptionType.OTHER)));

    mockMvc.perform(get("/api/subscriptions").header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].latitude").value(12.93));
  }

  @Test
  void listMineReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/subscriptions"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unsubscribeDelegatesToService() throws Exception {
    mockMvc.perform(delete("/api/subscriptions/10").header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isOk());

    verify(subscriptionService).unsubscribe(7L, 10L);
  }

  @Test
  void unsubscribeReturns404ForUnknownSubscription() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"))
        .when(subscriptionService).unsubscribe(anyLong(), anyLong());

    mockMvc.perform(delete("/api/subscriptions/99").header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renameReturnsUpdatedSubscription() throws Exception {
    when(subscriptionService.update(7L, 10L, new UpdateSubscriptionRequest("Home", null)))
        .thenReturn(new SubscriptionResponse(10L, "Home", 3L, "Koramangala", 12.93, 77.62, 150, true, SubscriptionType.OTHER));

    mockMvc.perform(patch("/api/subscriptions/10")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Home\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Home"))
        .andExpect(jsonPath("$.areaName").value("Koramangala"));
  }

  @Test
  void renameReturns400ForInvalidName() throws Exception {
    when(subscriptionService.update(eq(7L), eq(10L), any())).thenThrow(
        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required"));

    mockMvc.perform(patch("/api/subscriptions/10")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateAcceptsTypeAndReturnsIt() throws Exception {
    when(subscriptionService.update(7L, 10L, new UpdateSubscriptionRequest(null, SubscriptionType.SCHOOL)))
        .thenReturn(new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true,
            SubscriptionType.SCHOOL));

    mockMvc.perform(patch("/api/subscriptions/10")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"SCHOOL\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("SCHOOL"));
  }

  @Test
  void updateReturns400ForUnknownType() throws Exception {
    mockMvc.perform(patch("/api/subscriptions/10")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"CASTLE\"}"))
        .andExpect(status().isBadRequest());
    verify(subscriptionService, never()).update(anyLong(), anyLong(), any());
  }

  @Test
  void subscribeAreaPassesType() throws Exception {
    when(subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, 150, SubscriptionType.HOME)))
        .thenReturn(new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true,
            SubscriptionType.HOME));

    mockMvc.perform(post("/api/subscriptions/area")
            .header("Authorization", "Bearer " + tokenFor(7L))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"areaName\":\"Koramangala\",\"latitude\":12.93,\"longitude\":77.62,"
                + "\"aqiThreshold\":150,\"type\":\"HOME\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("HOME"));
  }

  @Test
  void renameReturns401WithoutToken() throws Exception {
    mockMvc.perform(patch("/api/subscriptions/10")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Home\"}"))
        .andExpect(status().isUnauthorized());
  }
}
