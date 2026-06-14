package com.clinic.clinic.security;

import com.clinic.clinic.global.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Writes a JSON {@link ExceptionResponse} with HTTP 401 for unauthenticated
 * requests (missing/invalid JWT) instead of letting Spring Security return an
 * empty body, so the Angular SPA can read the status and render its own page.
 * CORS headers are added earlier in the chain by Spring Security's CORS filter.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ExceptionResponse body = ExceptionResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .errorMessage("Authentication required")
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now().toString())
            .build();
        objectMapper.writeValue(response.getWriter(), body);
    }
}
