package github.atharv3221.airhealth.repository;

import java.util.List;

import github.atharv3221.airhealth.entity.Subscription;
import github.atharv3221.airhealth.entity.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  List<Subscription> findByUserId(Long userId);

  List<Subscription> findByActiveTrueAndType(SubscriptionType type);
}
