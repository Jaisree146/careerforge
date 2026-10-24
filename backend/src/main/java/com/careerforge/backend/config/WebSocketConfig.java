package com.careerforge.backend.config;

import com.careerforge.backend.modules.notification.websocket.JwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket & STOMP Message Broker configuration.
 * Enables full-duplex real-time communication for instant user notification delivery,
 * application state updates, and public announcement broadcasts.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS fallback endpoint for web browsers
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Raw WebSocket endpoint for mobile, Postman, and native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: broadcast to all subscribers
        // /queue: point-to-point private user messages (/user/{email}/queue/notifications)
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages bound for @MessageMapping handler methods
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-targeted destinations
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Enforce JWT token authentication on STOMP CONNECT frame
        registration.interceptors(jwtChannelInterceptor);
    }
}
