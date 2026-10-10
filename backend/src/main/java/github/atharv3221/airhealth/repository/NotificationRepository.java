package github.atharv3221.airhealth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import github.atharv3221.airhealth.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  // true if an alert was already created for this subscription after the given time
  boolean existsBySubscriptionIdAndCreatedAtAfter(Long subscriptionId, Instant since);

  List<Notification> findBySubscriptionUserIdOrderByCreatedAtDesc(Long userId);

  Optional<Notification> findByIdAndSubscriptionUserId(Long id, Long userId);

  long countBySubscriptionUserIdAndReadFalse(Long userId);
}
