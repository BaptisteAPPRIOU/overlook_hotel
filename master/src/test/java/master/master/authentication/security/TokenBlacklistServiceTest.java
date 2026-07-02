package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import master.master.config.JwtProperties;
import master.master.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class TokenBlacklistServiceTest {

  private static final long JWT_EXPIRATION_MS = 86_400_000;

  private StringRedisTemplate redisTemplate;
  private ValueOperations<String, String> valueOperations;
  private TokenBlacklistService tokenBlacklistService;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    redisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
    valueOperations = org.mockito.Mockito.mock(ValueOperations.class);
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setExpirationMs(JWT_EXPIRATION_MS);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    tokenBlacklistService = new TokenBlacklistService(redisTemplate, jwtProperties);
  }

  // Type: Unit test.
  // Verifies that a token is treated as usable
  // before it has been blacklisted by logout.
  @Test
  void tokenIsNotBlacklistedBeforeLogout() {
    when(redisTemplate.hasKey(anyString())).thenReturn(false);

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isFalse();
  }

  // Type: Unit test.
  // Verifies that a token becomes unusable after blacklisting
  // and is stored in Redis with a JWT-lifetime TTL.
  @Test
  void blacklistedTokenCanNoLongerBeUsed() {
    when(redisTemplate.hasKey(anyString())).thenReturn(true);

    tokenBlacklistService.blacklist("jwt-token");

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isTrue();
    verify(valueOperations)
        .set(
            argThat(key -> key.startsWith("auth:blacklist:")),
            eq("1"),
            eq(Duration.ofMillis(JWT_EXPIRATION_MS)));
  }
}
