package com.careerforge.backend.modules.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String websiteUrl;

    private Long locationId;

    public CompanyRequest() {
    }

    public CompanyRequest(String name, String description, String websiteUrl, Long locationId) {
        this.name = name;
        this.description = description;
        this.websiteUrl = websiteUrl;
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
