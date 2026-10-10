package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.AirQualityResponse;
import github.atharv3221.airhealth.dto.NotificationResponse;
import github.atharv3221.airhealth.entity.Area;
import github.atharv3221.airhealth.entity.Notification;
import github.atharv3221.airhealth.entity.Subscription;
import github.atharv3221.airhealth.repository.NotificationRepository;
import github.atharv3221.airhealth.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private static final long COOLDOWN_MINUTES = 360;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private AirService airService;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService(notificationRepository, subscriptionRepository,
        airService, COOLDOWN_MINUTES);
  }

  private static Area area(String name, double lat, double lon) {
    Area area = new Area();
    area.setName(name);
    area.setLatitude(lat);
    area.setLongitude(lon);
    return area;
  }

  private static Subscription subscription(long id, Area area, int threshold) {
    Subscription subscription = new Subscription();
    subscription.setArea(area);
    subscription.setAqiThreshold(threshold);
    ReflectionTestUtils.setField(subscription, "id", id);
    return subscription;
  }

  private static Notification notification(long id, Subscription subscription, String message,
                                           boolean read, Instant createdAt) {
    Notification notification = new Notification();
    notification.setSubscription(subscription);
    notification.setMessage(message);
    notification.setRead(read);
    ReflectionTestUtils.setField(notification, "id", id);
    ReflectionTestUtils.setField(notification, "createdAt", createdAt);
    return notification;
  }

  private static AirQualityResponse air(Integer aqi) {
    return new AirQualityResponse(new AirQualityResponse.Current(
        "2026-10-10T12:00", aqi, null, null, null, null, null, null));
  }

  @Test
  void listMineCreatesAlertWhenAqiAtOrAboveThreshold() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenReturn(air(150));

    notificationService.listMine(7L);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertThat(captor.getValue().getSubscription()).isSameAs(sub);
    assertThat(captor.getValue().getMessage())
        .isEqualTo("AQI in Koramangala is 150, at or above your limit of 150.");
    assertThat(captor.getValue().isRead()).isFalse();
  }

  @Test
  void noAlertWhenAqiBelowThreshold() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenReturn(air(149));

    notificationService.listMine(7L);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void noAlertWhenAqiMissing() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenReturn(air(null));

    notificationService.listMine(7L);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void skipsSubscriptionInCooldownWithoutCallingAirService() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(true);

    notificationService.listMine(7L);

    verify(airService, never()).getCurrent(anyDouble(), anyDouble());
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void cooldownCutoffUsesConfiguredMinutes() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(true);

    notificationService.listMine(7L);

    ArgumentCaptor<Instant> cutoff = ArgumentCaptor.forClass(Instant.class);
    verify(notificationRepository).existsBySubscriptionIdAndCreatedAtAfter(eq(10L), cutoff.capture());
    assertThat(Duration.between(cutoff.getValue(), Instant.now()).toMinutes())
        .isCloseTo(COOLDOWN_MINUTES, within(1L));
  }

  @Test
  void skipsSubscriptionWithoutArea() {
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L))
        .thenReturn(List.of(subscription(10L, null, 150)));

    notificationService.listMine(7L);

    verify(notificationRepository, never()).existsBySubscriptionIdAndCreatedAtAfter(any(), any());
    verify(airService, never()).getCurrent(anyDouble(), anyDouble());
    verify(notificationRepository, never()).save(any());
  }

  @Test
  void airServiceFailureSkipsOnlyThatSubscription() {
    Subscription failing = subscription(10L, area("Koramangala", 12.93, 77.62), 100);
    Subscription working = subscription(11L, area("Indiranagar", 12.97, 77.64), 100);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(failing, working));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(any(), any())).thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenThrow(new RuntimeException("Open-Meteo down"));
    when(airService.getCurrent(12.97, 77.64)).thenReturn(air(180));
    Notification existing = notification(1L, working, "old", true, Instant.parse("2026-10-09T10:00:00Z"));
    when(notificationRepository.findBySubscriptionUserIdOrderByCreatedAtDesc(7L))
        .thenReturn(List.of(existing));

    List<NotificationResponse> result = notificationService.listMine(7L);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertThat(captor.getValue().getSubscription()).isSameAs(working);
    assertThat(result).hasSize(1);
  }

  @Test
  void listMineMapsNotificationsToResponses() {
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of());
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    Instant newer = Instant.parse("2026-10-10T10:00:00Z");
    Instant older = Instant.parse("2026-10-09T10:00:00Z");
    when(notificationRepository.findBySubscriptionUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(
        notification(2L, sub, "second", false, newer),
        notification(1L, sub, "first", true, older)));

    assertThat(notificationService.listMine(7L)).containsExactly(
        new NotificationResponse(2L, 10L, "second", false, newer),
        new NotificationResponse(1L, 10L, "first", true, older));
  }

  @Test
  void unreadCountChecksAlertsThenCounts() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 100);
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenReturn(air(200));
    when(notificationRepository.countBySubscriptionUserIdAndReadFalse(7L)).thenReturn(3L);

    assertThat(notificationService.unreadCount(7L)).isEqualTo(3L);
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  void markReadSetsReadFlag() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    Instant createdAt = Instant.parse("2026-10-10T10:00:00Z");
    Notification unread = notification(5L, sub, "alert", false, createdAt);
    when(notificationRepository.findByIdAndSubscriptionUserId(5L, 7L)).thenReturn(Optional.of(unread));

    NotificationResponse response = notificationService.markRead(7L, 5L);

    assertThat(response).isEqualTo(new NotificationResponse(5L, 10L, "alert", true, createdAt));
    assertThat(unread.isRead()).isTrue();
  }

  @Test
  void markReadThrowsNotFoundForUnknownOrOtherUsersNotification() {
    when(notificationRepository.findByIdAndSubscriptionUserId(5L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markRead(7L, 5L))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void alertMessageUsesSubscriptionNameWhenSet() {
    Subscription sub = subscription(10L, area("Koramangala", 12.93, 77.62), 150);
    sub.setName("Home");
    when(subscriptionRepository.findByUserIdAndActiveTrue(7L)).thenReturn(List.of(sub));
    when(notificationRepository.existsBySubscriptionIdAndCreatedAtAfter(eq(10L), any()))
        .thenReturn(false);
    when(airService.getCurrent(12.93, 77.62)).thenReturn(air(160));

    notificationService.listMine(7L);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertThat(captor.getValue().getMessage())
        .isEqualTo("AQI in Home is 160, at or above your limit of 150.");
  }
}
