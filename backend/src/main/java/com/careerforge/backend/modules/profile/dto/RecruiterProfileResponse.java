package com.careerforge.backend.modules.profile.dto;

import com.careerforge.backend.modules.company.dto.CompanyResponse;

import java.time.Instant;

public class RecruiterProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private CompanyResponse company;
    private Instant createdAt;
    private Instant updatedAt;

    public RecruiterProfileResponse() {
    }

    public RecruiterProfileResponse(Long userId, String name, String email, CompanyResponse company, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.company = company;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CompanyResponse getCompany() {
        return company;
    }

    public void setCompany(CompanyResponse company) {
        this.company = company;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
