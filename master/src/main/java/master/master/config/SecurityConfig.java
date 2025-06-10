package master.master.config;

// Import necessary classes for security configuration
import master.master.filter.JwtAuthenticationFilter;
import master.master.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Marks this class as a configuration class
@EnableWebSecurity // Enables Spring Security’s web security support
@EnableMethodSecurity // Enables method-level security with annotations (like @PreAuthorize)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor injection for custom user service and JWT filter
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Defines the security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection (useful for stateless REST APIs)
                .authorizeHttpRequests(auth -> auth
                        // Allow access without authentication to public endpoints
                        .requestMatchers("/api/v1/login", "/api/v1/register", "/error").permitAll()
                        .requestMatchers("/", "/clientLogin", "/employeeLogin", "/register", "/employeeDashboard", "/roomManagement").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/image/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before the username-password authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Disable form login and HTTP Basic authentication
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();
    }

    // Defines a password encoder bean using BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Defines the authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder)
            throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder); // Set custom user service and encoder
        return builder.build();
    }
}
