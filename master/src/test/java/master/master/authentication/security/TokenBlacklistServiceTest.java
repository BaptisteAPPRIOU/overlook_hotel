package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;

import master.master.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

  @Test
  void tokenIsNotBlacklistedBeforeLogout() {
    TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isFalse();
  }

  @Test
  void blacklistedTokenCanNoLongerBeUsed() {
    TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    tokenBlacklistService.blacklist("jwt-token");

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isTrue();
    assertThat(tokenBlacklistService.isBlacklisted("another-token")).isFalse();
  }
}
