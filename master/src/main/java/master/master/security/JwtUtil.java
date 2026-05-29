package master.master.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import master.master.config.JwtProperties;

@Component
public class JwtUtil {

  private final Key signingKey;
  private final long expirationTime;

  /**
   * Builds the JWT helper from application properties.
   */
  public JwtUtil(JwtProperties jwtProperties) {
    // HS256 requires a sufficiently long shared secret converted to an HMAC signing key.
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationTime = jwtProperties.getExpirationMs();
  }

  /**
   * Returns the key used to sign and verify JWT tokens.
   */
  private Key getSigningKey() {
    return signingKey;
  }

  /**
   * Generates a signed JWT token for the given user email.
   */
  public String generateToken(String email) {
    Date now = new Date();
    return Jwts.builder()
        // The subject claim stores the email used by Spring Security as the username.
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationTime))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Extracts the username stored in the token subject.
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date stored in the token claims.
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim using the provided resolver function.
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Parses the token and returns all claims after signature verification.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Checks whether the token expiration date is already in the past.
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Validates that the token belongs to the given user and has not expired.
   */
  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  /**
   * Validates the token signature and expiration without loading user details.
   */
  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      // Invalid signatures, malformed tokens, and null/empty values are treated as unauthenticated.
      return false;
    }
  }
}
