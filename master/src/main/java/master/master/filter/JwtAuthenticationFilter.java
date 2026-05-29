// src/main/java/master/master/security/JwtAuthenticationFilter.java
package master.master.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import master.master.security.JwtUtil;
import master.master.security.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads JWT tokens from incoming requests and authenticates valid, non-blacklisted tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Autowired private TokenBlacklistService tokenBlacklistService;

  /**
   * Extracts a JWT from the request, validates it, and stores the authenticated user in the
   * Spring Security context.
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    String jwtToken = null;
    String email = null;

    // The Authorization header is the preferred location for API clients.
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwtToken = authHeader.substring(7);
    }

    // Browser flows can also send the JWT through the jwtToken cookie.
    if (jwtToken == null && request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("jwtToken".equals(cookie.getName())) {
          jwtToken = cookie.getValue();
          break;
        }
      }
    }

    // Validate the token before trusting any value stored inside its claims.
    if (jwtToken != null && jwtUtil.isTokenValid(jwtToken)) {
      // Blacklisted tokens are usually tokens that were explicitly logged out.
      if (tokenBlacklistService.isBlacklisted(jwtToken)) {
        filterChain.doFilter(request, response);
        return;
      }

      // The username claim stores the user's email address in this application.
      email = jwtUtil.extractUsername(jwtToken);
    }

    // Avoid replacing an authentication that was already set earlier in the filter chain.
    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      if (jwtUtil.isTokenValid(jwtToken)) {
        // Spring Security uses this token to expose the current user and their authorities.
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Request details include metadata such as the remote address and session id.
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}
