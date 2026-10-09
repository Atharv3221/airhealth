package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.dto.CreateUserRequest;
import github.atharv3221.airhealth.dto.UserResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordEncoder);
  }

  private static User user(long id, String name, String email) {
    User user = new User();
    user.setName(name);
    user.setEmail(email);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  @Test
  void createSavesUserWithHashedPassword() {
    when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(inv -> {
      User saved = inv.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", 1L);
      return saved;
    });

    UserResponse response = userService.create(new CreateUserRequest("Asha", "a@x.com", "secret"));

    assertThat(response).isEqualTo(new UserResponse(1L, "Asha", "a@x.com"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getPasswordHash()).isNotEqualTo("secret");
    assertThat(passwordEncoder.matches("secret", saved.getPasswordHash())).isTrue();
  }

  @Test
  void createRejectsDuplicateEmailWithConflict() {
    when(userRepository.findByEmail("a@x.com"))
        .thenReturn(Optional.of(user(1L, "Existing", "a@x.com")));

    assertThatThrownBy(() -> userService.create(new CreateUserRequest("Asha", "a@x.com", "secret")))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    verify(userRepository, never()).save(any());
  }

  @Test
  void getReturnsUser() {
    when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L, "Asha", "a@x.com")));

    assertThat(userService.get(7L)).isEqualTo(new UserResponse(7L, "Asha", "a@x.com"));
  }

  @Test
  void getThrowsNotFoundForUnknownId() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.get(99L))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
  }
}
