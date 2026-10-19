package com.careerforge.backend.modules.application.dto;

import java.time.Instant;

public class SavedJobResponse {

    private Long jobId;
    private String title;
    private String companyName;
    private String locationCity;
    private String salary;
    private String employmentType;
    private Instant savedAt;

    public SavedJobResponse() {
    }

    public SavedJobResponse(
            Long jobId,
            String title,
            String companyName,
            String locationCity,
            String salary,
            String employmentType,
            Instant savedAt
    ) {
        this.jobId = jobId;
        this.title = title;
        this.companyName = companyName;
        this.locationCity = locationCity;
        this.salary = salary;
        this.employmentType = employmentType;
        this.savedAt = savedAt;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Instant savedAt) {
        this.savedAt = savedAt;
    }
}
