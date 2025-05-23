package master.master.config;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Custom service to load user details for authentication
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Configure HTTP security rules and filters
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API simplicity (adjust if needed)
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to login, register, error pages, and static resources
                        .requestMatchers("/api/v1/login", "/api/v1/register", "/error").permitAll()
                        .requestMatchers("/", "/clientLogin", "/employeeLogin", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/image/**").permitAll()
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                // Disable default form login and HTTP basic authentication
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();
    }

    // Password encoder bean using BCrypt hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configure AuthenticationManager with user details service and password encoder
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder)
            throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
