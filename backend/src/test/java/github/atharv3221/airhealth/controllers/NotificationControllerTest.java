package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.JwtService;
import github.atharv3221.airhealth.dto.NotificationResponse;
import github.atharv3221.airhealth.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private NotificationService notificationService;

  private String tokenFor(long id) {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", id);
    return jwtService.generateToken(user);
  }

  @Test
  void listMineReturnsNotifications() throws Exception {
    when(notificationService.listMine(7L)).thenReturn(List.of(new NotificationResponse(
        5L, 10L, "AQI in Koramangala is 180", false, Instant.parse("2026-10-10T10:00:00Z"))));

    mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(5))
        .andExpect(jsonPath("$[0].subscriptionId").value(10))
        .andExpect(jsonPath("$[0].message").value("AQI in Koramangala is 180"))
        .andExpect(jsonPath("$[0].read").value(false));
  }

  @Test
  void listMineReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/notifications"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unreadCountReturnsCountObject() throws Exception {
    when(notificationService.unreadCount(7L)).thenReturn(3L);

    mockMvc.perform(get("/api/notifications/unread-count")
            .header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(3));
  }

  @Test
  void markReadReturnsUpdatedNotification() throws Exception {
    when(notificationService.markRead(7L, 5L)).thenReturn(new NotificationResponse(
        5L, 10L, "alert", true, Instant.parse("2026-10-10T10:00:00Z")));

    mockMvc.perform(patch("/api/notifications/5/read")
            .header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.read").value(true));
  }

  @Test
  void markReadReturns404ForOtherUsersNotification() throws Exception {
    when(notificationService.markRead(7L, 5L)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

    mockMvc.perform(patch("/api/notifications/5/read")
            .header("Authorization", "Bearer " + tokenFor(7L)))
        .andExpect(status().isNotFound());
  }
}
