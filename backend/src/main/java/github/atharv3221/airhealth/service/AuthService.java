package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.LoginRequest;
import github.atharv3221.airhealth.dto.LoginResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final github.atharv3221.airhealth.service.JwtService jwtService;

  public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     github.atharv3221.airhealth.service.JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .filter(u -> u.getPasswordHash() != null
            && passwordEncoder.matches(request.password(), u.getPasswordHash()))
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    return new LoginResponse(jwtService.generateToken(user));
  }
}
