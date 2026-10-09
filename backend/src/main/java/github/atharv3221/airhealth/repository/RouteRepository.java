package github.atharv3221.airhealth.repository;

import github.atharv3221.airhealth.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {}
