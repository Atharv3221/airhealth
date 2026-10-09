package github.atharv3221.airhealth.repository;

import java.util.Optional;

import github.atharv3221.airhealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

}
