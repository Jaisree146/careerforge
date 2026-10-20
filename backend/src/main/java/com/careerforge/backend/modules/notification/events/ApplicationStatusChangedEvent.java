package com.careerforge.backend.modules.notification.events;

import com.careerforge.backend.modules.application.model.ApplicationStatus;

public class ApplicationStatusChangedEvent {

    private final Long applicationId;
    private final Long candidateId;
    private final Long recruiterId;
    private final Long jobId;
    private final String jobTitle;
    private final ApplicationStatus newStatus;
    private final String notes;

    public ApplicationStatusChangedEvent(Long applicationId, Long candidateId, Long recruiterId, Long jobId, String jobTitle, ApplicationStatus newStatus, String notes) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.recruiterId = recruiterId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.newStatus = newStatus;
        this.notes = notes;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public String getNotes() {
        return notes;
    }
}
