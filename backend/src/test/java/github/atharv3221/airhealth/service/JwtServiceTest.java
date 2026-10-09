package github.atharv3221.airhealth.service;

import github.atharv3221.airhealth.TestJwt;
import github.atharv3221.airhealth.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

  private final JwtService jwtService = TestJwt.jwtService();

  private static User user(long id) {
    User user = new User();
    user.setEmail("a@x.com");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  @Test
  void tokenRoundTripsUserId() {
    String token = jwtService.generateToken(user(42L));

    assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
  }

  @Test
  void rejectsTokenSignedWithDifferentKey() {
    JwtService other = new JwtService("a-completely-different-secret-of-32-bytes", 60000);
    String token = other.generateToken(user(42L));

    assertThatThrownBy(() -> jwtService.extractUserId(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void rejectsExpiredToken() {
    JwtService expired = new JwtService(TestJwt.SECRET, -1000);
    String token = expired.generateToken(user(42L));

    assertThatThrownBy(() -> jwtService.extractUserId(token))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void rejectsGarbageToken() {
    assertThatThrownBy(() -> jwtService.extractUserId("not-a-jwt"))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void rejectsSecretShorterThan32Bytes() {
    assertThatThrownBy(() -> new JwtService("too-short", 60000))
        .isInstanceOf(WeakKeyException.class);
  }
}
