package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.TestJwt;
import github.atharv3221.airhealth.dto.LoginRequest;
import github.atharv3221.airhealth.dto.LoginResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final JwtService jwtService = TestJwt.jwtService();

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordEncoder, jwtService);
  }

  private User user(long id, String email, String rawPassword) {
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(rawPassword == null ? null : passwordEncoder.encode(rawPassword));
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private static void assertUnauthorized(Runnable call) {
    assertThatThrownBy(call::run)
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
  }

  @Test
  void loginReturnsTokenForUserIdOnCorrectPassword() {
    when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(user(7L, "a@x.com", "secret")));

    LoginResponse response = authService.login(new LoginRequest("a@x.com", "secret"));

    assertThat(jwtService.extractUserId(response.token())).isEqualTo(7L);
  }

  @Test
  void loginRejectsWrongPassword() {
    when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(user(7L, "a@x.com", "secret")));

    assertUnauthorized(() -> authService.login(new LoginRequest("a@x.com", "wrong")));
  }

  @Test
  void loginRejectsUnknownEmail() {
    when(userRepository.findByEmail("nobody@x.com")).thenReturn(Optional.empty());

    assertUnauthorized(() -> authService.login(new LoginRequest("nobody@x.com", "secret")));
  }

  @Test
  void loginRejectsUserWithoutPassword() {
    when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(user(7L, "a@x.com", null)));

    assertUnauthorized(() -> authService.login(new LoginRequest("a@x.com", "anything")));
  }
}
