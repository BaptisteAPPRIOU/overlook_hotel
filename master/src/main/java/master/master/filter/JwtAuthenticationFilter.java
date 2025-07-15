package master.master.filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import master.master.security.JwtUtil;
import master.master.service.CustomUserDetailsService;

/**
 * JWT Authentication Filter that extends OncePerRequestFilter to ensure it's executed once per request.
 * This filter intercepts HTTP requests to validate JWT tokens and establish security context.
 * 
 * <p>The filter performs the following operations:
 * <ul>
 *   <li>Extracts JWT token from the Authorization header (Bearer token format)</li>
 *   <li>Validates the token using JwtUtil</li>
 *   <li>Extracts user email from the valid token</li>
 *   <li>Loads user details and creates authentication context</li>
 *   <li>Sets the authentication in SecurityContextHolder for the current request</li>
 * </ul>
 * 
 * <p>This filter is executed before other security filters in the Spring Security filter chain
 * and is responsible for authenticating users based on JWT tokens provided in request headers.
 * 
 * @author Your Name
 * @version 1.0
 * @since 1.0
 * 
 * @see OncePerRequestFilter
 * @see JwtUtil
 * @see CustomUserDetailsService
 * @see SecurityContextHolder
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;

        // First, check Authorization header for "Bearer " token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }
        
        // If no token in header, check cookies
        if (jwtToken == null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        // Validate token and extract email
        if (jwtToken != null && jwtUtil.isTokenValid(jwtToken)) {
            email = jwtUtil.extractEmail(jwtToken);
        }

        // If email is extracted and not already authenticated, authenticate the request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isTokenValid(jwtToken)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
