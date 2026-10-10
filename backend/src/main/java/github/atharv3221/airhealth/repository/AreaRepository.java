package github.atharv3221.airhealth.repository;

import java.util.Optional;

import github.atharv3221.airhealth.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

  Optional<Area> findByLatitudeAndLongitude(double latitude, double longitude);

}
