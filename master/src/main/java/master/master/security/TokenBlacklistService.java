package master.master.security;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistService {

  // Concurrent storage keeps blacklist checks safe when multiple requests logout or authenticate.
  private final Set<String> blacklistedTokens =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  /**
   * Adds a token to the blacklist so it can no longer authenticate requests.
   */
  public void blacklist(String token) {
    blacklistedTokens.add(token);
  }

  /**
   * Checks whether a token was explicitly invalidated.
   */
  public boolean isBlacklisted(String token) {
    return blacklistedTokens.contains(token);
  }
}
