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

  public JwtUtil(JwtProperties jwtProperties) {
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    this.expirationTime = jwtProperties.getExpirationMs();
  }

  // This method retrieves the signing key used for JWT.
  private Key getSigningKey() {
    return signingKey;
  }

  // This method generates a JWT token for the given email.
  public String generateToken(String email) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationTime))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  // This method extracts the username (email) from the JWT token.
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // This method extracts the expiration date from the JWT token.
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // This method extracts a specific claim from the JWT token.
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // This method extracts all claims from the JWT token.
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // This method checks if the token is expired.
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // This method validates the token against the user details.
  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  // This method checks if the token is valid without needing user details.
  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
