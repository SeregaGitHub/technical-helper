package ru.kraser.technical_helper.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import ru.kraser.technical_helper.common_module.enums.Role;

import java.util.Collections;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityWebConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${allowed.frontend.url}")
    private String allowedFrontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(BASE_URL + AUTH_URL)
                        .permitAll()
                        .requestMatchers(BASE_URL + DEFAULT_URL)
                        .permitAll()
                        .requestMatchers(BASE_URL + ADMIN_URL + "/**").hasAnyAuthority(
                                Role.ADMIN.name())

                        .requestMatchers(BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/**").hasAnyAuthority(
                                Role.EMPLOYEE.name(), Role.TECHNICIAN.name(), Role.ADMIN.name())

                        .requestMatchers(BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + "/**").hasAnyAuthority(
                                Role.TECHNICIAN.name(), Role.ADMIN.name())

                        .requestMatchers(BASE_URL + BREAKAGE_URL + ADMIN_URL + "/**").hasAnyAuthority(
                                Role.ADMIN.name())

                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider).addFilterAfter(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedOrigins(List.of(allowedFrontendUrl));
                cfg.setAllowedMethods(Collections.singletonList("*"));
                cfg.setAllowedHeaders(Collections.singletonList("*"));
                cfg.setExposedHeaders(Collections.singletonList("*"));
                cfg.setMaxAge(3600L);
                return cfg;
            }
        };
    }
}
