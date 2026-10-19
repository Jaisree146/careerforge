package com.careerforge.backend.modules.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SavedJobId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "job_id")
    private Long jobId;

    public SavedJobId() {
    }

    public SavedJobId(Long userId, Long jobId) {
        this.userId = userId;
        this.jobId = jobId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedJobId that = (SavedJobId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, jobId);
    }
}
