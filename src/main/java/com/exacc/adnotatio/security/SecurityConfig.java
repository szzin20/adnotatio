package com.exacc.adnotatio.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API uses per-request Basic credentials and never relies on a browser session cookie.
                // Keep CSRF enabled if this API is later used by a browser-based client.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/products").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/*")
                        .hasAnyRole("ADMIN", "CUSTOMER", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/products")
                        .hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/*")
                        .hasRole("SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/*")
                        .hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/*")
                        .hasRole("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/*/cart/items")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers/*/cart/items")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/customers/*/cart/items/*")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/*/cart/items/*")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers/*/orders")
                        .hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/*")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/customers", "/api/v1/customers/**")
                        .hasRole("ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication diperlukan"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Akses ditolak")));

        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"adnotatio\"");
        }
        // These messages are fixed application strings; no request data is reflected into the body.
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"data\":null}");
    }
}
