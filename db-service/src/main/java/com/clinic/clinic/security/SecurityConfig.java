package com.clinic.clinic.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // CSRF is disabled because this is a stateless, token-based API: there is no
                // session/cookie, so there is no CSRF attack surface. Credentials travel in the
                // Authorization: Bearer header, which is not automatically attached by browsers.
                // NOTE: the /save/**, /get/**, /delete/** routes below are permitAll by design for
                // internal service-to-service calls (no JWT is forwarded). They are exposed without
                // authorization — restrict them at the network layer or add service auth if needed.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                "/auth/**",
                                "/save/user",
                                "/save/userConfirm",
                                "/save/token",
                                "/save/appointment",
                                "/get/availableAppointments",
                                "delete/appointment",
                                "delete/doctorFromAppointment",
                                "get/myAppointments",
                                "get/ownAppointments",
                                "get/reviews",
                                "save/review",
                                "get/user",
                                "get/appointment",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/vi",
                                "/configuration/security",
                                "/swagger-vi/**",
                                "/webjars/**" ,
                                "/swagger-ui.html"
                        ).permitAll()
                                .anyRequest()
                                .authenticated()
                ).sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
