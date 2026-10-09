package github.atharv3221.airhealth;

import github.atharv3221.airhealth.service.JwtService;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

// gives plain unit tests the same mock JWT config that Spring tests get from config/application.properties
public final class TestJwt {

  private static final Properties PROPS = load();

  public static final String SECRET = PROPS.getProperty("jwt.secret");
  public static final long EXPIRATION_MS = Long.parseLong(PROPS.getProperty("jwt.expiration-ms"));

  private TestJwt() {}

  public static JwtService jwtService() {
    return new JwtService(SECRET, EXPIRATION_MS);
  }

  private static Properties load() {
    try (InputStream in = TestJwt.class.getResourceAsStream("/config/application.properties")) {
      Properties props = new Properties();
      props.load(in);
      return props;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
