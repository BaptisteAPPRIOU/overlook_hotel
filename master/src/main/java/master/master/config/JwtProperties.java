package master.master.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  private String secret;
  // Default token validity is 24 hours when no value is provided in application properties.
  private long expirationMs = 86_400_000;

  /**
   * Returns the secret key used to sign and validate JWT tokens.
   */
  public String getSecret() {
    return secret;
  }

  /**
   * Updates the secret key loaded from the app.jwt.secret configuration property.
   */
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * Returns the configured JWT expiration time in milliseconds.
   */
  public long getExpirationMs() {
    return expirationMs;
  }

  /**
   * Updates the JWT expiration time loaded from the app.jwt.expiration-ms property.
   */
  public void setExpirationMs(long expirationMs) {
    this.expirationMs = expirationMs;
  }
}
