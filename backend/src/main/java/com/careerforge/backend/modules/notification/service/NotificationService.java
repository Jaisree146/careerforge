package com.careerforge.backend.modules.notification.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.notification.dto.AuditLogResponse;
import com.careerforge.backend.modules.notification.dto.NotificationResponse;
import com.careerforge.backend.modules.notification.dto.UnreadCountResponse;
import com.careerforge.backend.modules.notification.model.AuditLog;
import com.careerforge.backend.modules.notification.model.Notification;
import com.careerforge.backend.modules.notification.model.NotificationType;
import com.careerforge.backend.modules.notification.repository.AuditLogRepository;
import com.careerforge.backend.modules.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponse createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type,
            Long referenceId,
            String referenceType
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Notification notification = new Notification(user, title, message, type, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);

        return mapToNotificationResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToNotificationResponse);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized: Notification belongs to another user");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return mapToNotificationResponse(updated);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void recordAuditLog(Long userId, String action, String entityType, Long entityId, String details) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        AuditLog auditLog = new AuditLog(user, action, entityType, entityId, details);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToAuditLogResponse);
    }

    private NotificationResponse mapToNotificationResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getReferenceId(),
                n.getReferenceType(),
                n.isRead(),
                n.getCreatedAt()
        );
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getUser() != null ? a.getUser().getId() : null,
                a.getUser() != null ? a.getUser().getName() : "SYSTEM",
                a.getUser() != null ? a.getUser().getEmail() : null,
                a.getAction(),
                a.getEntityType(),
                a.getEntityId(),
                a.getDetails(),
                a.getCreatedAt()
        );
    }
}
