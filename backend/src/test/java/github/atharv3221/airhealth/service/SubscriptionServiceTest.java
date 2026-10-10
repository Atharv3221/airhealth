package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.CreateSubscriptionRequest;
import github.atharv3221.airhealth.dto.SubscriptionResponse;
import github.atharv3221.airhealth.dto.UpdateSubscriptionRequest;
import github.atharv3221.airhealth.entity.Area;
import github.atharv3221.airhealth.entity.Subscription;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.repository.AreaRepository;
import github.atharv3221.airhealth.repository.SubscriptionRepository;
import github.atharv3221.airhealth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private AreaRepository areaRepository;

  @Mock
  private UserRepository userRepository;

  private SubscriptionService subscriptionService;

  @BeforeEach
  void setUp() {
    subscriptionService = new SubscriptionService(subscriptionRepository, areaRepository, userRepository);
  }

  private static User user(long id) {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private static Area area(long id, String name, double lat, double lon) {
    Area area = new Area();
    area.setName(name);
    area.setLatitude(lat);
    area.setLongitude(lon);
    ReflectionTestUtils.setField(area, "id", id);
    return area;
  }

  private static Subscription subscription(long id, User user, Area area, int threshold, boolean active) {
    Subscription subscription = new Subscription();
    subscription.setUser(user);
    subscription.setArea(area);
    subscription.setAqiThreshold(threshold);
    subscription.setActive(active);
    ReflectionTestUtils.setField(subscription, "id", id);
    return subscription;
  }

  private void saveAssignsId(long id) {
    when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
      Subscription saved = inv.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", id);
      return saved;
    });
  }

  @Test
  void createReusesExistingAreaAndSavesSubscription() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L)).thenReturn(Optional.empty());
    when(userRepository.getReferenceById(7L)).thenReturn(user(7L));
    saveAssignsId(10L);

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, 150));

    assertThat(response).isEqualTo(
        new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true));
    verify(areaRepository, never()).save(any());

    ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
    verify(subscriptionRepository).save(captor.capture());
    assertThat(captor.getValue().getUser().getId()).isEqualTo(7L);
    assertThat(captor.getValue().getArea()).isSameAs(koramangala);
  }

  @Test
  void createRoundsCoordinatesAndCreatesNewArea() {
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.empty());
    when(areaRepository.save(any(Area.class))).thenAnswer(inv -> {
      Area saved = inv.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", 4L);
      return saved;
    });
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 4L)).thenReturn(Optional.empty());
    when(userRepository.getReferenceById(7L)).thenReturn(user(7L));
    saveAssignsId(11L);

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.9312, 77.6249, 150));

    assertThat(response).isEqualTo(
        new SubscriptionResponse(11L, "Koramangala", 4L, "Koramangala", 12.93, 77.62, 150, true));

    ArgumentCaptor<Area> captor = ArgumentCaptor.forClass(Area.class);
    verify(areaRepository).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("Koramangala");
    assertThat(captor.getValue().getLatitude()).isEqualTo(12.93);
    assertThat(captor.getValue().getLongitude()).isEqualTo(77.62);
  }

  @Test
  void createUsesDefaultThresholdWhenNotGiven() {
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62))
        .thenReturn(Optional.of(area(3L, "Koramangala", 12.93, 77.62)));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L)).thenReturn(Optional.empty());
    when(userRepository.getReferenceById(7L)).thenReturn(user(7L));
    saveAssignsId(10L);

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, null));

    assertThat(response.aqiThreshold()).isEqualTo(100);
  }

  @Test
  void createRejectsDuplicateActiveSubscriptionWithConflict() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L))
        .thenReturn(Optional.of(subscription(10L, user(7L), koramangala, 100, true)));

    assertThatThrownBy(() -> subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, 150)))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  void createReactivatesInactiveSubscriptionWithNewThreshold() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    Subscription inactive = subscription(10L, user(7L), koramangala, 100, false);
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L)).thenReturn(Optional.of(inactive));

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, 150));

    assertThat(response).isEqualTo(
        new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true));
    assertThat(inactive.isActive()).isTrue();
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  void createReactivationKeepsOldThresholdWhenNotGiven() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L))
        .thenReturn(Optional.of(subscription(10L, user(7L), koramangala, 120, false)));

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Koramangala", 12.93, 77.62, null));

    assertThat(response.aqiThreshold()).isEqualTo(120);
    assertThat(response.active()).isTrue();
  }

  @Test
  void listMineReturnsOnlyActiveSubscriptions() {
    User me = user(7L);
    when(subscriptionRepository.findByUserId(7L)).thenReturn(List.of(
        subscription(10L, me, area(3L, "Koramangala", 12.93, 77.62), 150, true),
        subscription(11L, me, area(4L, "Indiranagar", 12.97, 77.64), 100, false)));

    assertThat(subscriptionService.listMine(7L)).containsExactly(
        new SubscriptionResponse(10L, "Koramangala", 3L, "Koramangala", 12.93, 77.62, 150, true));
  }

  @Test
  void unsubscribeDeactivatesOwnSubscription() {
    Subscription mine = subscription(10L, user(7L), area(3L, "Koramangala", 12.93, 77.62), 150, true);
    when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(mine));

    subscriptionService.unsubscribe(7L, 10L);

    assertThat(mine.isActive()).isFalse();
  }

  @Test
  void unsubscribeThrowsNotFoundForOtherUsersSubscription() {
    Subscription theirs = subscription(10L, user(8L), area(3L, "Koramangala", 12.93, 77.62), 150, true);
    when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(theirs));

    assertThatThrownBy(() -> subscriptionService.unsubscribe(7L, 10L))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    assertThat(theirs.isActive()).isTrue();
  }

  @Test
  void unsubscribeThrowsNotFoundForUnknownId() {
    when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.unsubscribe(7L, 99L))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void createKeepsUsersNameSeparateFromSharedAreaName() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L)).thenReturn(Optional.empty());
    when(userRepository.getReferenceById(7L)).thenReturn(user(7L));
    saveAssignsId(10L);

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("  Home  ", 12.93, 77.62, 150));

    assertThat(response).isEqualTo(
        new SubscriptionResponse(10L, "Home", 3L, "Koramangala", 12.93, 77.62, 150, true));
    assertThat(koramangala.getName()).isEqualTo("Koramangala");
  }

  @Test
  void createRejectsTooLongName() {
    assertThatThrownBy(() -> subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("x".repeat(121), 12.93, 77.62, 150)))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    verify(subscriptionRepository, never()).save(any());
  }

  @Test
  void createReactivationUpdatesName() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    Subscription inactive = subscription(10L, user(7L), koramangala, 100, false);
    inactive.setName("Old name");
    when(areaRepository.findByLatitudeAndLongitude(12.93, 77.62)).thenReturn(Optional.of(koramangala));
    when(subscriptionRepository.findByUserIdAndAreaId(7L, 3L)).thenReturn(Optional.of(inactive));

    SubscriptionResponse response = subscriptionService.createAreaSubscription(7L,
        new CreateSubscriptionRequest("Office", 12.93, 77.62, null));

    assertThat(response.name()).isEqualTo("Office");
  }

  @Test
  void renameSetsTrimmedNameWithoutTouchingArea() {
    Area koramangala = area(3L, "Koramangala", 12.93, 77.62);
    Subscription mine = subscription(10L, user(7L), koramangala, 150, true);
    when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(mine));

    SubscriptionResponse response = subscriptionService.rename(7L, 10L,
        new UpdateSubscriptionRequest("  Parents' place "));

    assertThat(response).isEqualTo(new SubscriptionResponse(
        10L, "Parents' place", 3L, "Koramangala", 12.93, 77.62, 150, true));
    assertThat(mine.getName()).isEqualTo("Parents' place");
    assertThat(koramangala.getName()).isEqualTo("Koramangala");
  }

  @Test
  void renameRejectsBlankOrTooLongName() {
    for (UpdateSubscriptionRequest bad : new UpdateSubscriptionRequest[] {
        null, new UpdateSubscriptionRequest(null), new UpdateSubscriptionRequest("   "),
        new UpdateSubscriptionRequest("x".repeat(121))}) {
      assertThatThrownBy(() -> subscriptionService.rename(7L, 10L, bad))
          .isInstanceOfSatisfying(ResponseStatusException.class,
              e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
    verify(subscriptionRepository, never()).findById(any());
  }

  @Test
  void renameThrowsNotFoundForOtherUsersSubscription() {
    Subscription theirs = subscription(10L, user(8L), area(3L, "Koramangala", 12.93, 77.62), 150, true);
    when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(theirs));

    assertThatThrownBy(() -> subscriptionService.rename(7L, 10L, new UpdateSubscriptionRequest("Mine now")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    assertThat(theirs.getName()).isNull();
  }

  @Test
  void renameThrowsNotFoundForUnsubscribed() {
    Subscription inactive = subscription(10L, user(7L), area(3L, "Koramangala", 12.93, 77.62), 150, false);
    when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(inactive));

    assertThatThrownBy(() -> subscriptionService.rename(7L, 10L, new UpdateSubscriptionRequest("Home")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
  }
}
