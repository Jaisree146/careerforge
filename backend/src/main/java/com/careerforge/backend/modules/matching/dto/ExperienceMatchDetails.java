package com.careerforge.backend.modules.matching.dto;

public class ExperienceMatchDetails {

    private double candidateExperienceYears;
    private double requiredExperienceYears;
    private String requiredExperienceText;
    private boolean meetsRequirement;
    private double experienceScore;

    public ExperienceMatchDetails() {
    }

    public ExperienceMatchDetails(double candidateExperienceYears, double requiredExperienceYears, String requiredExperienceText, boolean meetsRequirement, double experienceScore) {
        this.candidateExperienceYears = candidateExperienceYears;
        this.requiredExperienceYears = requiredExperienceYears;
        this.requiredExperienceText = requiredExperienceText;
        this.meetsRequirement = meetsRequirement;
        this.experienceScore = experienceScore;
    }

    public double getCandidateExperienceYears() {
        return candidateExperienceYears;
    }

    public void setCandidateExperienceYears(double candidateExperienceYears) {
        this.candidateExperienceYears = candidateExperienceYears;
    }

    public double getRequiredExperienceYears() {
        return requiredExperienceYears;
    }

    public void setRequiredExperienceYears(double requiredExperienceYears) {
        this.requiredExperienceYears = requiredExperienceYears;
    }

    public String getRequiredExperienceText() {
        return requiredExperienceText;
    }

    public void setRequiredExperienceText(String requiredExperienceText) {
        this.requiredExperienceText = requiredExperienceText;
    }

    public boolean isMeetsRequirement() {
        return meetsRequirement;
    }

    public void setMeetsRequirement(boolean meetsRequirement) {
        this.meetsRequirement = meetsRequirement;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }
}
