package com.ootd.fitme.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode code = ErrorCode.AUTH_UNAUTHORIZED;
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                code.getCode(),
                code.getMessage(),
                Map.of("path", request.getRequestURI()),
                authException.getClass().getSimpleName(),
                code.getStatus().value()
        );

        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
