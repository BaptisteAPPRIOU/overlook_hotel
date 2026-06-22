package master.master.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  private String secret;
  private long expirationMs = 86_400_000;

  // Return the JWT signing secret.
  public String getSecret() {
    return secret;
  }

  // Store the JWT signing secret.
  public void setSecret(String secret) {
    this.secret = secret;
  }

  // Return the configured token lifetime.
  public long getExpirationMs() {
    return expirationMs;
  }

  // Store the configured token lifetime.
  public void setExpirationMs(long expirationMs) {
    this.expirationMs = expirationMs;
  }
}
