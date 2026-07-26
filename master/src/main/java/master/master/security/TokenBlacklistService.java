package master.master.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import master.master.config.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistService {

  private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:";
  private static final String BLACKLIST_VALUE = "1";

  private final StringRedisTemplate redisTemplate;
  private final Duration blacklistTtl;

  public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
    this.redisTemplate = redisTemplate;
    this.blacklistTtl = Duration.ofMillis(jwtProperties.getExpirationMs());
  }

  public void blacklist(String token) {
    redisTemplate.opsForValue().set(tokenKey(token), BLACKLIST_VALUE, blacklistTtl);
  }

  public boolean isBlacklisted(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey(token)));
  }

  private String tokenKey(String token) {
    return BLACKLIST_KEY_PREFIX + sha256(token);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }
}
