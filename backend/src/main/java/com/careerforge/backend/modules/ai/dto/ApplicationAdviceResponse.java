package com.careerforge.backend.modules.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAdviceResponse {

    private String jobTitle;
    private double currentMatchScore;
    private List<String> strengthsHighlight = new ArrayList<>();
    private List<String> areasToImprove = new ArrayList<>();
    private List<String> recommendedSkillsToLearn = new ArrayList<>();
    private String coverLetterDraft;

    public ApplicationAdviceResponse() {
    }

    public ApplicationAdviceResponse(String jobTitle, double currentMatchScore, List<String> strengthsHighlight, List<String> areasToImprove, List<String> recommendedSkillsToLearn, String coverLetterDraft) {
        this.jobTitle = jobTitle;
        this.currentMatchScore = currentMatchScore;
        this.strengthsHighlight = strengthsHighlight;
        this.areasToImprove = areasToImprove;
        this.recommendedSkillsToLearn = recommendedSkillsToLearn;
        this.coverLetterDraft = coverLetterDraft;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public double getCurrentMatchScore() {
        return currentMatchScore;
    }

    public void setCurrentMatchScore(double currentMatchScore) {
        this.currentMatchScore = currentMatchScore;
    }

    public List<String> getStrengthsHighlight() {
        return strengthsHighlight;
    }

    public void setStrengthsHighlight(List<String> strengthsHighlight) {
        this.strengthsHighlight = strengthsHighlight;
    }

    public List<String> getAreasToImprove() {
        return areasToImprove;
    }

    public void setAreasToImprove(List<String> areasToImprove) {
        this.areasToImprove = areasToImprove;
    }

    public List<String> getRecommendedSkillsToLearn() {
        return recommendedSkillsToLearn;
    }

    public void setRecommendedSkillsToLearn(List<String> recommendedSkillsToLearn) {
        this.recommendedSkillsToLearn = recommendedSkillsToLearn;
    }

    public String getCoverLetterDraft() {
        return coverLetterDraft;
    }

    public void setCoverLetterDraft(String coverLetterDraft) {
        this.coverLetterDraft = coverLetterDraft;
    }
}
