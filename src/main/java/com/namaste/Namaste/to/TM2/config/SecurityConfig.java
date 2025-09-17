package com.namaste.Namaste.to.TM2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.terminology.public:true}")  // Default to true for development
    private boolean allowPublicTerminologyAccess;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
                .authorizeHttpRequests(authz -> {
                    // ALLOW ALL ENDPOINTS FOR TESTING
                    authz.requestMatchers("/api/fhir/**").permitAll()
                            .requestMatchers("/api/terminology/**").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/actuator/health").permitAll()
                            .anyRequest().permitAll(); // Allow all other requests too
                })
                .headers(headers ->
                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin()) // For H2 console
                );

        return http.build();
    }
}