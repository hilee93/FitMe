package com.ootd.fitme.global.security.jwt;

import com.ootd.fitme.global.security.auth.CustomUserDetailsService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DM_SUBSCRIBE_PREFIX = "/sub/direct-messages_";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handlerConnect(accessor);
        }
        else if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscribeDestination(accessor.getDestination(), accessor.getUser());
        }
        return message;
    }

    // stomp 헤더 토큰 추출
    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    // 토큰 (CONNECT) 검증
    private void handlerConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (!jwtProvider.validateToken(token) || !jwtProvider.isAccessToken(token)) {
            throw new IllegalArgumentException("토큰이 유효하지 않습니다.");
        }
        UUID userId = jwtProvider.getUserId(token);
        String jti = jwtProvider.getTokenId(token);
        Instant iat = jwtProvider.getIssuedAt(token);

        if(tokenBlacklistService.isBlacklisted(jti)){
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        Instant cutoff = tokenBlacklistService.getRevokeAllBefore(userId);
        if (cutoff != null && iat.isBefore(cutoff)) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        UserDetails userDetails = userDetailsService.loadUserById(userId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                (userDetails, null, userDetails.getAuthorities());
        accessor.setUser(authentication);
    }

    // SUBSCRIBE 권한 검증
    private void validateSubscribeDestination(String destination, Principal principal){
        if (destination == null || !destination.startsWith(DM_SUBSCRIBE_PREFIX)) {
            return;
        }

        String dmKey = destination.substring(DM_SUBSCRIBE_PREFIX.length());
        String[] part = dmKey.split("_");
        if (part.length != 2) {
            throw new IllegalArgumentException("잘못된 채널 형식입니다.");
        }
        UUID channelUserId1;
        UUID channelUserId2;

        try {
            channelUserId1 = UUID.fromString(part[0]);
            channelUserId2 = UUID.fromString(part[1]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 채널 형식입니다.");
        }

        if(!(principal instanceof UsernamePasswordAuthenticationToken auth)) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
        UUID authUserId = userPrincipal.getUserId();

        if (!authUserId.equals(channelUserId1) && !authUserId.equals(channelUserId2)) {
            throw new AccessDeniedException("해당 채널에 접근 권한이 없습니다.");
        }
    }
}
