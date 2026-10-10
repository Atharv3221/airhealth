package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.CreateSubscriptionRequest;
import github.atharv3221.airhealth.dto.SubscriptionResponse;
import github.atharv3221.airhealth.dto.UpdateSubscriptionRequest;
import github.atharv3221.airhealth.entity.Area;
import github.atharv3221.airhealth.entity.Subscription;
import github.atharv3221.airhealth.repository.AreaRepository;
import github.atharv3221.airhealth.repository.SubscriptionRepository;
import github.atharv3221.airhealth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

  static final int MAX_NAME_LENGTH = 120;

  private final SubscriptionRepository subscriptionRepository;
  private final AreaRepository areaRepository;
  private final UserRepository userRepository;

  public SubscriptionService(SubscriptionRepository subscriptionRepository,
                             AreaRepository areaRepository,
                             UserRepository userRepository) {
    this.subscriptionRepository = subscriptionRepository;
    this.areaRepository = areaRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public SubscriptionResponse createAreaSubscription(Long userId,
          CreateSubscriptionRequest request) {
    String name = cleanName(request.areaName());
    double lat = round(request.latitude());
    double lon = round(request.longitude());

    Area area = areaRepository.findByLatitudeAndLongitude(lat, lon).orElseGet(() -> {
      Area created = new Area();
      created.setName(request.areaName());
      created.setLatitude(lat);
      created.setLongitude(lon);
      return areaRepository.save(created);
    });

    Optional<Subscription> existing = subscriptionRepository
        .findByUserIdAndAreaId(userId, area.getId());
    if (existing.isPresent()) {
      Subscription found = existing.get();
      if (found.isActive()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed to this area");
      }
      found.setActive(true);
      if (name != null) {
        found.setName(name);
      }
      if (request.aqiThreshold() != null) {
        found.setAqiThreshold(request.aqiThreshold());
      }
      return toResponse(found);
    }

    Subscription subscription = new Subscription();
    subscription.setUser(userRepository.getReferenceById(userId));
    subscription.setArea(area);
    subscription.setName(name);
    if (request.aqiThreshold() != null) {
      subscription.setAqiThreshold(request.aqiThreshold());
    }
    return toResponse(subscriptionRepository.save(subscription));
  }

  @Transactional(readOnly = true)
  public List<SubscriptionResponse> listMine(Long userId) {
    return subscriptionRepository.findByUserId(userId).stream()
        .filter(Subscription::isActive)
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public SubscriptionResponse rename(Long userId, Long subscriptionId,
          UpdateSubscriptionRequest request) {
    String name = cleanName(request == null ? null : request.name());
    if (name == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
    }
    Subscription subscription = findOwned(userId, subscriptionId);
    // unsubscribed ones are hidden from the user, so treat them as missing
    if (!subscription.isActive()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found");
    }
    subscription.setName(name);
    return toResponse(subscription);
  }

  @Transactional
  public void unsubscribe(Long userId, Long subscriptionId) {
    findOwned(userId, subscriptionId).setActive(false);
  }

  private Subscription findOwned(Long userId, Long subscriptionId) {
    return subscriptionRepository.findById(subscriptionId)
        .filter(s -> s.getUser().getId().equals(userId))
        .orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
  }

  // trimmed name, or null when blank; too long is a 400
  private String cleanName(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String name = raw.trim();
    if (name.length() > MAX_NAME_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Name must be at most " + MAX_NAME_LENGTH + " characters");
    }
    return name;
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private SubscriptionResponse toResponse(Subscription s) {
    Area a = s.getArea();
    String name = s.getName() != null ? s.getName() : a.getName();
    return new SubscriptionResponse(s.getId(), name, a.getId(), a.getName(),
        a.getLatitude(), a.getLongitude(), s.getAqiThreshold(), s.isActive());
  }
}
