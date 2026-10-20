package com.careerforge.backend.modules.notification.service;

import com.careerforge.backend.modules.notification.events.ApplicationStatusChangedEvent;
import com.careerforge.backend.modules.notification.events.ApplicationSubmittedEvent;
import com.careerforge.backend.modules.notification.model.NotificationType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // 1. Notify recruiter
        notificationService.createNotification(
                event.getRecruiterId(),
                "New Job Application Received",
                String.format("Candidate %s submitted an application for your posting: '%s'", event.getCandidateName(), event.getJobTitle()),
                NotificationType.APPLICATION_SUBMITTED,
                event.getApplicationId(),
                "APPLICATION"
        );

        // 2. Notify candidate with confirmation
        notificationService.createNotification(
                event.getCandidateId(),
                "Application Confirmed",
                String.format("Your application for '%s' was submitted successfully and is now under review.", event.getJobTitle()),
                NotificationType.APPLICATION_SUBMITTED,
                event.getApplicationId(),
                "APPLICATION"
        );

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

        notificationService.createNotification(
                event.getCandidateId(),
                "Application Status Update",
                String.format("Your application status for '%s' has been updated to %s.%s", event.getJobTitle(), event.getNewStatus(), feedback),
                NotificationType.APPLICATION_STATUS_UPDATED,
                event.getApplicationId(),
                "APPLICATION"
        );

        // 2. Record audit trail
        notificationService.recordAuditLog(
                event.getRecruiterId(),
                "APPLICATION_STATUS_UPDATED",
                "APPLICATION",
                event.getApplicationId(),
                String.format("Status changed to %s for Job ID %d ('%s')", event.getNewStatus(), event.getJobId(), event.getJobTitle())
        );
    }
}
