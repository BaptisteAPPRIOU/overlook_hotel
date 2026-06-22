package master.master.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import master.master.config.JwtProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility component for creating and validating JWT tokens.
 * It centralizes token parsing, claim extraction, and signature verification.
 */
@Component
public class JwtUtil {

  private final Key signingKey;
  private final long expirationTime;

  // Builds the JWT utility from the configured secret and expiration window.
  public JwtUtil(JwtProperties jwtProperties) {
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationTime = jwtProperties.getExpirationMs();
  }

  // Returns the signing key used for JWT creation and parsing.
  private Key getSigningKey() {
    return signingKey;
  }

  // Generates a signed JWT for the supplied email address.
  public String generateToken(String email) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationTime))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  // Extracts the subject, which is used as the username or email.
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // Extracts the token expiration timestamp.
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // Extracts a single claim using the provided resolver function.
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // Parses the token and returns all claims after signature verification.
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // Checks whether the token expiration date is already in the past.
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // Validates that the token belongs to the expected user and is still valid.
  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  // Verifies the token signature and expiration without comparing user details.
  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
