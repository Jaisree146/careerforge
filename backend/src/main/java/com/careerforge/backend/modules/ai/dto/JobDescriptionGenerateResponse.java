package com.careerforge.backend.modules.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class JobDescriptionGenerateResponse {

    private String title;
    private String description;
    private List<String> suggestedRequiredSkills = new ArrayList<>();
    private List<String> suggestedOptionalSkills = new ArrayList<>();
    private String recommendedSalaryRange;

    public JobDescriptionGenerateResponse() {
    }

    public JobDescriptionGenerateResponse(String title, String description, List<String> suggestedRequiredSkills, List<String> suggestedOptionalSkills, String recommendedSalaryRange) {
        this.title = title;
        this.description = description;
        this.suggestedRequiredSkills = suggestedRequiredSkills;
        this.suggestedOptionalSkills = suggestedOptionalSkills;
        this.recommendedSalaryRange = recommendedSalaryRange;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSuggestedRequiredSkills() {
        return suggestedRequiredSkills;
    }

    public void setSuggestedRequiredSkills(List<String> suggestedRequiredSkills) {
        this.suggestedRequiredSkills = suggestedRequiredSkills;
    }

    public List<String> getSuggestedOptionalSkills() {
        return suggestedOptionalSkills;
    }

    public void setSuggestedOptionalSkills(List<String> suggestedOptionalSkills) {
        this.suggestedOptionalSkills = suggestedOptionalSkills;
    }

    public String getRecommendedSalaryRange() {
        return recommendedSalaryRange;
    }

    public void setRecommendedSalaryRange(String recommendedSalaryRange) {
        this.recommendedSalaryRange = recommendedSalaryRange;
    }
}
