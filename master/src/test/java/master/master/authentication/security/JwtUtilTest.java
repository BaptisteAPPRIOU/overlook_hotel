package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;

import master.master.config.JwtProperties;
import master.master.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtUtilTest {

  // Type: Unit test.
  // Verifies that a generated JWT contains the user email
  // and is accepted by the token validation helpers.
  @Test
  void generatedTokenContainsUserEmailAndIsValid() {
    JwtUtil jwtUtil = jwtUtilWithExpiration(86_400_000);
    UserDetails userDetails =
        User.withUsername("client@olh.fr").password("hashed-password").authorities("CLIENT").build();

    String token = jwtUtil.generateToken("client@olh.fr");

    assertThat(jwtUtil.extractUsername(token)).isEqualTo("client@olh.fr");
    assertThat(jwtUtil.isTokenValid(token)).isTrue();
    assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
  }

  // Type: Unit test.
  // Verifies that a JWT generated for one email
  // is rejected when validated against another user.
  @Test
  void tokenIsRejectedWhenItBelongsToAnotherUser() {
    JwtUtil jwtUtil = jwtUtilWithExpiration(86_400_000);
    UserDetails otherUser =
        User.withUsername("other@olh.fr").password("hashed-password").authorities("CLIENT").build();

    String token = jwtUtil.generateToken("client@olh.fr");

    assertThat(jwtUtil.validateToken(token, otherUser)).isFalse();
  }

  // Type: Unit test.
  // Verifies that an expired JWT is not considered valid
  // by the token validation helper.
  @Test
  void expiredTokenIsNotValid() {
    JwtUtil jwtUtil = jwtUtilWithExpiration(-1);

    String token = jwtUtil.generateToken("client@olh.fr");

    assertThat(jwtUtil.isTokenValid(token)).isFalse();
  }

  // Type: Unit test.
  // Verifies that malformed token text is rejected
  // without throwing an error to the caller.
  @Test
  void malformedTokenIsNotValid() {
    JwtUtil jwtUtil = jwtUtilWithExpiration(86_400_000);

    assertThat(jwtUtil.isTokenValid("not-a-jwt-token")).isFalse();
  }

  private JwtUtil jwtUtilWithExpiration(long expirationMs) {
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setSecret("test_secret_key_with_at_least_32_chars");
    jwtProperties.setExpirationMs(expirationMs);
    return new JwtUtil(jwtProperties);
  }
}
