package master.master.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import master.master.filter.JwtAuthenticationFilter;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

  private JwtUtil jwtUtil;
  private UserDetailsService userDetailsService;
  private TokenBlacklistService tokenBlacklistService;
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    jwtUtil = org.mockito.Mockito.mock(JwtUtil.class);
    userDetailsService = org.mockito.Mockito.mock(UserDetailsService.class);
    tokenBlacklistService = org.mockito.Mockito.mock(TokenBlacklistService.class);
    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    ReflectionTestUtils.setField(jwtAuthenticationFilter, "tokenBlacklistService", tokenBlacklistService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // Type: Unit test.
  // Verifies that a valid Bearer token authenticates the request
  // and stores the loaded user authorities in the security context.
  @Test
  void bearerTokenAuthenticatesRequestWhenTokenIsValid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer jwt-token");
    UserDetails userDetails =
        User.withUsername("client@olh.fr").password("hashed-password").authorities("CLIENT").build();

    when(jwtUtil.isTokenValid("jwt-token")).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted("jwt-token")).thenReturn(false);
    when(jwtUtil.extractUsername("jwt-token")).thenReturn("client@olh.fr");
    when(userDetailsService.loadUserByUsername("client@olh.fr")).thenReturn(userDetails);

    jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("client@olh.fr");
    assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
        .extracting("authority")
        .containsExactly("CLIENT");
  }

  // Type: Unit test.
  // Verifies that the JWT cookie is used for authentication
  // when the Authorization header is not present.
  @Test
  void jwtCookieAuthenticatesRequestWhenAuthorizationHeaderIsMissing() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("jwtToken", "cookie-token"));
    UserDetails userDetails =
        User.withUsername("client@olh.fr").password("hashed-password").authorities("CLIENT").build();

    when(jwtUtil.isTokenValid("cookie-token")).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted("cookie-token")).thenReturn(false);
    when(jwtUtil.extractUsername("cookie-token")).thenReturn("client@olh.fr");
    when(userDetailsService.loadUserByUsername("client@olh.fr")).thenReturn(userDetails);

    jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("client@olh.fr");
  }

  // Type: Unit test.
  // Verifies that a blacklisted token is ignored
  // and does not load user details or authenticate the request.
  @Test
  void blacklistedTokenDoesNotAuthenticateRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer jwt-token");

    when(jwtUtil.isTokenValid("jwt-token")).thenReturn(true);
    when(tokenBlacklistService.isBlacklisted("jwt-token")).thenReturn(true);

    jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(userDetailsService, never()).loadUserByUsername("client@olh.fr");
  }

  // Type: Unit test.
  // Verifies that an invalid token leaves the request unauthenticated
  // and does not load user details.
  @Test
  void invalidTokenDoesNotAuthenticateRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer invalid-token");

    when(jwtUtil.isTokenValid("invalid-token")).thenReturn(false);

    jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(userDetailsService, never()).loadUserByUsername("client@olh.fr");
  }
}
