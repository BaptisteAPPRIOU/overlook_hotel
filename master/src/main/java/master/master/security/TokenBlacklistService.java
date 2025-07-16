package master.master.security;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // This method adds a token to the blacklist.
    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    // This method checks if a token is blacklisted.
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
