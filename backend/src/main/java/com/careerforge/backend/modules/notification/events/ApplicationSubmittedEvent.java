package com.careerforge.backend.modules.notification.events;

public class ApplicationSubmittedEvent {

    private final Long applicationId;
    private final Long candidateId;
    private final String candidateName;
    private final Long recruiterId;
    private final Long jobId;
    private final String jobTitle;

    public ApplicationSubmittedEvent(Long applicationId, Long candidateId, String candidateName, Long recruiterId, Long jobId, String jobTitle) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.recruiterId = recruiterId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public String getCandidateName() {
        return candidateName;
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
}
