package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.AirQualityResponse;
import github.atharv3221.airhealth.dto.NotificationResponse;
import github.atharv3221.airhealth.entity.Area;
import github.atharv3221.airhealth.entity.Notification;
import github.atharv3221.airhealth.entity.Subscription;
import github.atharv3221.airhealth.repository.NotificationRepository;
import github.atharv3221.airhealth.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final AirService airService;
  private final long cooldownMinutes;

  public NotificationService(NotificationRepository notificationRepository,
                             SubscriptionRepository subscriptionRepository,
                             AirService airService,
                             @Value("${alert.cooldown-minutes}") long cooldownMinutes) {
    this.notificationRepository = notificationRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.airService = airService;
    this.cooldownMinutes = cooldownMinutes;
  }

  @Transactional
  public List<NotificationResponse> listMine(Long userId) {
    checkAlerts(userId);
    return notificationRepository.
        findBySubscriptionUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public long unreadCount(Long userId) {
    checkAlerts(userId);
    return notificationRepository.countBySubscriptionUserIdAndReadFalse(userId);
  }

  @Transactional
  public NotificationResponse markRead(Long userId, Long id) {
    Notification notification = notificationRepository.findByIdAndSubscriptionUserId(id, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    notification.setRead(true);
    return toResponse(notification);
  }

  private void checkAlerts(Long userId) {
    Instant cutoff = Instant.now().minus(cooldownMinutes, ChronoUnit.MINUTES);

    for (Subscription subscription : subscriptionRepository.findByUserIdAndActiveTrue(userId)) {
      Area area = subscription.getArea();
      if (area == null) {
        continue;
      }
      if (notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(subscription.getId(), cutoff)) {
        continue;
      }
      try {
        AirQualityResponse air = airService.getCurrent(area.getLatitude(), area.getLongitude());
        Integer aqi = air.current().usAqi();
        if (aqi != null && aqi >= subscription.getAqiThreshold()) {
          Notification notification = new Notification();
          notification.setSubscription(subscription);
          notification.setMessage("AQI in " + area.getName() + " is " + aqi
              + ", at or above your limit of " + subscription.getAqiThreshold() + ".");
          notificationRepository.save(notification);
        }
      } catch (RuntimeException e) {
        // Open-Meteo unavailable: skip this subscription, still return the list
      }
    }
  }

  private NotificationResponse toResponse(Notification n) {
    return new NotificationResponse(n.getId(), n.getSubscription().getId(),
        n.getMessage(), n.isRead(), n.getCreatedAt());
  }
}
