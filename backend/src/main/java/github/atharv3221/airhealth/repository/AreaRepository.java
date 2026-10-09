package github.atharv3221.airhealth.repository;

import github.atharv3221.airhealth.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {}
