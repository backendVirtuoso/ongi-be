package com.ongi.infra.config;

import com.ongi.infra.jwt.JwtAuthFilter;
import com.ongi.infra.security.AdminAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AdminAuthFilter adminAuthFilter;

    @org.springframework.beans.factory.annotation.Value("${ongi.frontend.base-url}")
    private String frontendBaseUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(frontendBaseUrl.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public - Auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Public - Quotes (read)
                        .requestMatchers(HttpMethod.GET, "/api/v1/quotes/**").permitAll()
                        // Protected - AI generate (로그인 필수)
                        .requestMatchers(HttpMethod.POST, "/api/v1/quotes/ai-generate").authenticated()
                        // Public - Subscribe
                        .requestMatchers(HttpMethod.POST, "/api/v1/subscribers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subscribers/verify").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/subscribers/**").permitAll()
                        // Protected - Like / Save toggle
                        .requestMatchers(HttpMethod.POST, "/api/v1/quotes/*/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/quotes/*/save").authenticated()
                        // Protected - My page
                        .requestMatchers("/api/v1/subscribers/me/**").authenticated()
                        // Admin - JWT + admin role 검증은 AdminAuthFilter가 처리
                        .requestMatchers("/api/v1/admin/**").authenticated()
                        // Actuator / Error
                        .requestMatchers("/actuator/**", "/error").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"success\":false,\"data\":null,\"message\":\"인증이 필요합니다.\"}");
                        })
                )
                .addFilterBefore(adminAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
