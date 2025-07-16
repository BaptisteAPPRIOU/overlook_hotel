package master.master.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import master.master.filter.JwtAuthenticationFilter;
import master.master.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/", "/clientLogin", "/employeeLogin", "/register", "employeeDashboard"
                        ).permitAll()
                        .requestMatchers(
                                "/css/**", "/js/**", "/image/**", "/favicon.ico"
                        ).permitAll()


                        .requestMatchers(
                                "/api/v1/login", "/api/v1/register", "/error"
                        ).permitAll()


                        .requestMatchers("/api/v1/logout").authenticated()


                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clients/**")
                        .hasAuthority("ADMIN")

                        // Employee Dashboard and related pages - Only EMPLOYEE and ADMIN can access
                        .requestMatchers("/employeeDashboard").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/roomManagement").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/planning").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/my-planning").hasAnyAuthority("EMPLOYEE", "ADMIN")

                        // Employee API endpoints - Only EMPLOYEE and ADMIN can access
                        .requestMatchers("/api/v1/employees/**").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/employees/**").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/dashboard/**").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers("/planning/**").hasAnyAuthority("EMPLOYEE", "ADMIN")

                        // Time tracking endpoints - Only EMPLOYEE and ADMIN can access
                        .requestMatchers("/api/v1/time-tracking/**").hasAnyAuthority("EMPLOYEE", "ADMIN")

                        // Room management API - Only EMPLOYEE and ADMIN can access
                        .requestMatchers("/api/v1/rooms/**").hasAnyAuthority("EMPLOYEE", "ADMIN")

                        // Client API endpoints
                        .requestMatchers("/api/v1/clients/**")
                        .hasAnyAuthority("CLIENT", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/v1/employees/**")
                        .hasAnyAuthority("EMPLOYEE", "ADMIN")

                        .requestMatchers("/api/v1/admin/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/v1/rooms/**")
                        .hasAnyAuthority("EMPLOYEE", "ADMIN")

                        // Fidelity point endpoints - Only CLIENT can access
                        .requestMatchers("/api/v1/fidelity/**")
                        .hasAuthority("CLIENT")

                        // Client pages - require CLIENT authority only
                        .requestMatchers("/clientHomePage", "/home", "/client/home", "/clientProfile")
                        .hasAuthority("CLIENT")

                        // toute autre requête doit être authentifiée
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Redirect to login page with error message for access denied
                            response.sendRedirect("/?error=access_denied");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Redirect to login page for unauthenticated requests
                            response.sendRedirect("/?error=not_authenticated");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // Bean for PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean for AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
