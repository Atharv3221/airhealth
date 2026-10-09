package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.CreateUserRequest;
import github.atharv3221.airhealth.dto.UserResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponse create(CreateUserRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    return toResponse(userRepository.save(user));
  }

  public UserResponse get(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return toResponse(user);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(user.getId(), user.getName(), user.getEmail());
  }
}
