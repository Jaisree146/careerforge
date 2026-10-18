package com.careerforge.backend.modules.profile.dto;

import jakarta.validation.constraints.NotNull;

public class RecruiterProfileRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    public RecruiterProfileRequest() {
    }

    public RecruiterProfileRequest(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
