package com.example.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/**").permitAll()

                // ADMIN only
                .requestMatchers(HttpMethod.POST, "/employees").hasAuthority("admin")
                .requestMatchers(HttpMethod.PUT, "/employees/**").hasAuthority("admin")
                .requestMatchers(HttpMethod.DELETE, "/employees/**").hasAuthority("admin")

                // All authenticated users can view
                .requestMatchers(HttpMethod.GET, "/employees").hasAnyAuthority("admin", "manager", "tester", "developer")
                .requestMatchers(HttpMethod.GET, "/employees/**").hasAnyAuthority("admin", "manager", "tester", "developer")

                // Everything else needs authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\": false, \"message\": \"Unauthorized - Please login first\"}");
                })
            );

        return http.build();
    }
}