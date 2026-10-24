package com.careerforge.backend.modules.notification.service;

import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.notification.dto.NotificationResponse;
import com.careerforge.backend.modules.notification.events.ApplicationStatusChangedEvent;
import com.careerforge.backend.modules.notification.events.ApplicationSubmittedEvent;
import com.careerforge.backend.modules.notification.model.NotificationType;
import com.careerforge.backend.modules.notification.websocket.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener that processes domain lifecycle events,
 * persists notifications and audit logs to MySQL, and immediately
 * dispatches real-time WebSocket push notifications to connected clients.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserRepository userRepository;

    public NotificationEventListener(NotificationService notificationService,
                                     WebSocketNotificationService webSocketNotificationService,
                                     UserRepository userRepository) {
        this.notificationService = notificationService;
        this.webSocketNotificationService = webSocketNotificationService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // 1. Notify recruiter
        NotificationResponse recruiterNotif = notificationService.createNotification(
                event.getRecruiterId(),
                "New Job Application Received",
                String.format("Candidate %s submitted an application for your posting: '%s'", event.getCandidateName(), event.getJobTitle()),
                NotificationType.APPLICATION_SUBMITTED,
                event.getApplicationId(),
                "APPLICATION"
        );
        sendRealTimeNotification(event.getRecruiterId(), recruiterNotif);

        // 2. Notify candidate with confirmation
        NotificationResponse candidateNotif = notificationService.createNotification(
                event.getCandidateId(),
                "Application Confirmed",
                String.format("Your application for '%s' was submitted successfully and is now under review.", event.getJobTitle()),
                NotificationType.APPLICATION_SUBMITTED,
                event.getApplicationId(),
                "APPLICATION"
        );
        sendRealTimeNotification(event.getCandidateId(), candidateNotif);

        // 3. Record audit trail
        notificationService.recordAuditLog(
                event.getCandidateId(),
                "APPLICATION_SUBMITTED",
                "APPLICATION",
                event.getApplicationId(),
                String.format("Applied to Job ID %d ('%s')", event.getJobId(), event.getJobTitle())
        );
    }

    @EventListener
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        // 1. Notify candidate
        String feedback = (event.getNotes() != null && !event.getNotes().isBlank())
                ? " Note from recruiter: " + event.getNotes()
                : "";

        NotificationResponse candidateNotif = notificationService.createNotification(
                event.getCandidateId(),
                "Application Status Update",
                String.format("Your application status for '%s' has been updated to %s.%s", event.getJobTitle(), event.getNewStatus(), feedback),
                NotificationType.APPLICATION_STATUS_UPDATED,
                event.getApplicationId(),
                "APPLICATION"
        );
        sendRealTimeNotification(event.getCandidateId(), candidateNotif);

        // 2. Record audit trail
        notificationService.recordAuditLog(
                event.getRecruiterId(),
                "APPLICATION_STATUS_UPDATED",
                "APPLICATION",
                event.getApplicationId(),
                String.format("Status changed to %s for Job ID %d ('%s')", event.getNewStatus(), event.getJobId(), event.getJobTitle())
        );
    }

    private void sendRealTimeNotification(Long userId, NotificationResponse notif) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                webSocketNotificationService.sendNotificationToUser(user.getEmail(), notif);
            });
        } catch (Exception ex) {
            log.warn("Failed to deliver real-time WebSocket notification to userId {}: {}", userId, ex.getMessage());
        }
    }
}
