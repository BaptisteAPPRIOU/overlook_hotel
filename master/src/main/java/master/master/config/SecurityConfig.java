package master.master.config;

import master.master.filter.JwtAuthenticationFilter;
import master.master.service.CustomUserDetailsService;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/", "/clientLogin", "/employeeLogin", "/register"
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

                        // toute autre requête doit être authentifiée
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
