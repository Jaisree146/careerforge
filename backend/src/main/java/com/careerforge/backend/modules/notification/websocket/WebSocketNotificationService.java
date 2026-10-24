package com.careerforge.backend.modules.notification.websocket;

import com.careerforge.backend.modules.notification.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for pushing real-time messages and notifications over WebSocket STOMP channels.
 */
@Service
public class WebSocketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a private real-time notification to a specific user's private queue (/user/{email}/queue/notifications).
     *
     * @param userEmail     Recipient user's authenticated principal username (email).
     * @param notification  Notification payload.
     */
    public void sendNotificationToUser(String userEmail, NotificationResponse notification) {
        log.info("Dispatching real-time WebSocket notification to user: {} on /queue/notifications", userEmail);
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", notification);
    }

    /**
     * Broadcasts a public announcement or event to all subscribed clients on /topic/{topic}.
     *
     * @param topic   Topic destination suffix (e.g. "announcements", "jobs").
     * @param payload Message payload.
     */
    public void broadcastToTopic(String topic, Object payload) {
        String destination = "/topic/" + topic;
        log.info("Broadcasting message over WebSocket topic: {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Convenience method for system announcements.
     */
    public void broadcastAnnouncement(String title, String message) {
        Map<String, Object> announcement = new LinkedHashMap<>();
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("timestamp", java.time.Instant.now());
        broadcastToTopic("announcements", announcement);
    }
}
