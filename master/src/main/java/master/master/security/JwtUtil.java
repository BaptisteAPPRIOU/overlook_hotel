package master.master.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for handling JSON Web Tokens (JWT) operations such as generation, extraction, and validation.
 * <p>
 * This class provides methods to generate JWT tokens for a given email, extract the email from a token,
 * and validate the authenticity and integrity of a token using a secret key.
 * </p>
 *
 * <ul>
 *   <li>{@link #generateToken(String)}: Generates a JWT token for the specified email.</li>
 *   <li>{@link #extractEmail(String)}: Extracts the email (subject) from the provided JWT token.</li>
 *   <li>{@link #isTokenValid(String)}: Validates the provided JWT token.</li>
 * </ul>
 *
 * <p>
 * The secret key used for signing and verifying tokens should be kept secure and at least 256 bits long.
 * The expiration time for tokens is set to 1 day (86400000 milliseconds).
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     JwtUtil jwtUtil = new JwtUtil();
 *     String token = jwtUtil.generateToken("user@example.com");
 *     String email = jwtUtil.extractEmail(token);
 *     boolean isValid = jwtUtil.isTokenValid(token);
 * </pre>
 * </p>
 *
 * @author Your Name
 */

@Component
public class JwtUtil {

    private final String SECRET_KEY = "mon_super_secret_jwt_key_123456789123456789"; // >= 256 bits
    private final long EXPIRATION_TIME = 86400000; // 1 day

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
