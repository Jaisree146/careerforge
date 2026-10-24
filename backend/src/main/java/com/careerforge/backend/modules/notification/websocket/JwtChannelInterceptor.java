package com.careerforge.backend.modules.notification.websocket;

import com.careerforge.backend.modules.auth.security.CustomUserDetailsService;
import com.careerforge.backend.modules.auth.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
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

/**
 * Channel interceptor that validates JWT tokens present in the native headers
 * of incoming STOMP CONNECT frames, authenticating the user's WebSocket session.
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtChannelInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                authHeader = accessor.getFirstNativeHeader("token");
                if (authHeader != null && !authHeader.startsWith("Bearer ")) {
                    authHeader = "Bearer " + authHeader;
                }
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    String userEmail = jwtService.extractEmail(jwt);
                    if (userEmail != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication);
                            log.info("WebSocket STOMP session authenticated for user: {}", userEmail);
                        } else {
                            log.warn("Invalid JWT token for WebSocket connection: {}", userEmail);
                            throw new AccessDeniedException("Expired or invalid JWT token");
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to authenticate STOMP CONNECT frame: {}", ex.getMessage());
                    throw new AccessDeniedException("Unauthorized WebSocket connection: " + ex.getMessage());
                }
            } else {
                log.warn("STOMP CONNECT frame missing Authorization Bearer token header");
                // Allow anonymous for public broadcast topics, or reject if strict
            }
        }

        return message;
    }
}
