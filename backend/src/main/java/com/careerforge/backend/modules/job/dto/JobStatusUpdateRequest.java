package com.careerforge.backend.modules.job.dto;

import com.careerforge.backend.modules.job.model.JobStatus;
import jakarta.validation.constraints.NotNull;

public class JobStatusUpdateRequest {

    @NotNull(message = "Job status is required")
    private JobStatus status;

    public JobStatusUpdateRequest() {
    }

    public JobStatusUpdateRequest(JobStatus status) {
        this.status = status;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
