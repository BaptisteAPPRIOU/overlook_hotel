package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;

import master.master.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

  // Type: Unit test.
  // Verifies that a token is treated as usable
  // before it has been blacklisted by logout.
  @Test
  void tokenIsNotBlacklistedBeforeLogout() {
    TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isFalse();
  }

  // Type: Unit test.
  // Verifies that a token becomes unusable after blacklisting
  // while other tokens remain unaffected.
  @Test
  void blacklistedTokenCanNoLongerBeUsed() {
    TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    tokenBlacklistService.blacklist("jwt-token");

    assertThat(tokenBlacklistService.isBlacklisted("jwt-token")).isTrue();
    assertThat(tokenBlacklistService.isBlacklisted("another-token")).isFalse();
  }
}
