package com.ootd.fitme.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorCode code = ErrorCode.AUTH_FORBIDDEN;
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                code.getCode(),
                code.getMessage(),
                Map.of("path", request.getRequestURI()),
                accessDeniedException.getClass().getSimpleName(),
                code.getStatus().value()
        );

        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
