package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.dto.NotificationResponse;
import github.atharv3221.airhealth.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public List<NotificationResponse> listMine(Authentication auth) {
    return notificationService.listMine(userId(auth));
  }

  @GetMapping("/unread-count")
  public Map<String, Long> unreadCount(Authentication auth) {
    return Map.of("count", notificationService.unreadCount(userId(auth)));
  }

  @PatchMapping("/{id}/read")
  public NotificationResponse markRead(Authentication auth, @PathVariable Long id) {
    return notificationService.markRead(userId(auth), id);
  }

  private Long userId(Authentication auth) {
    return Long.parseLong(auth.getName());
  }
}
