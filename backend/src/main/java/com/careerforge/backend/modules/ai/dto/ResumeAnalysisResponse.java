package com.careerforge.backend.modules.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumeAnalysisResponse {

    private String summary;
    private List<String> extractedSkills = new ArrayList<>();
    private String estimatedSeniorityLevel;
    private List<String> strengths = new ArrayList<>();
    private List<String> suggestedRoles = new ArrayList<>();

    public ResumeAnalysisResponse() {
    }

    public ResumeAnalysisResponse(String summary, List<String> extractedSkills, String estimatedSeniorityLevel, List<String> strengths, List<String> suggestedRoles) {
        this.summary = summary;
        this.extractedSkills = extractedSkills;
        this.estimatedSeniorityLevel = estimatedSeniorityLevel;
        this.strengths = strengths;
        this.suggestedRoles = suggestedRoles;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills;
    }

    public String getEstimatedSeniorityLevel() {
        return estimatedSeniorityLevel;
    }

    public void setEstimatedSeniorityLevel(String estimatedSeniorityLevel) {
        this.estimatedSeniorityLevel = estimatedSeniorityLevel;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getSuggestedRoles() {
        return suggestedRoles;
    }

    public void setSuggestedRoles(List<String> suggestedRoles) {
        this.suggestedRoles = suggestedRoles;
    }
}
