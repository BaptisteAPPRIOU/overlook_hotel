package master.master.security;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory blacklist for JWT tokens that have been invalidated on logout.
 * The set is concurrent so requests can check it safely from multiple threads.
 */
@Component
public class TokenBlacklistService {

  private final Set<String> blacklistedTokens =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  // Adds a token to the blacklist so it can no longer authenticate requests.
  public void blacklist(String token) {
    blacklistedTokens.add(token);
  }

  // Returns true when the token has already been blacklisted.
  public boolean isBlacklisted(String token) {
    return blacklistedTokens.contains(token);
  }
}
