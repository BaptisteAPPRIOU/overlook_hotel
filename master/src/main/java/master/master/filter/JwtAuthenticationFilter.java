package master.master.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import master.master.security.JwtUtil;
import master.master.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;

        // Le header Authorization doit commencer par "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            if (jwtUtil.isTokenValid(jwtToken)) {
                email = jwtUtil.extractEmail(jwtToken);
            }
        }

        // Si email est extrait et pas déjà authentifié, on authentifie la requête
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
