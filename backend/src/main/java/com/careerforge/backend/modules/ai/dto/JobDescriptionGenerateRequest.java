package com.careerforge.backend.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class JobDescriptionGenerateRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    private String companyName;
    private String experience;
    private String employmentType;
    private List<String> keyPoints = new ArrayList<>();

    public JobDescriptionGenerateRequest() {
    }

    public JobDescriptionGenerateRequest(String title, String companyName, String experience, String employmentType, List<String> keyPoints) {
        this.title = title;
        this.companyName = companyName;
        this.experience = experience;
        this.employmentType = employmentType;
        this.keyPoints = keyPoints;
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

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }
}
