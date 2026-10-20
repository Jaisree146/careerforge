package com.careerforge.backend.modules.matching.dto;

import java.util.ArrayList;
import java.util.List;

public class MatchScoreBreakdown {

    private double overallScore;
    private double skillScore;
    private double experienceScore;
    private double profileCompletenessScore;
    private String fitLevel;
    private SkillMatchDetails skillBreakdown;
    private ExperienceMatchDetails experienceBreakdown;
    private List<String> recommendationReasons = new ArrayList<>();

    public MatchScoreBreakdown() {
    }

    public MatchScoreBreakdown(
            double overallScore,
            double skillScore,
            double experienceScore,
            double profileCompletenessScore,
            String fitLevel,
            SkillMatchDetails skillBreakdown,
            ExperienceMatchDetails experienceBreakdown,
            List<String> recommendationReasons
    ) {
        this.overallScore = overallScore;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.profileCompletenessScore = profileCompletenessScore;
        this.fitLevel = fitLevel;
        this.skillBreakdown = skillBreakdown;
        this.experienceBreakdown = experienceBreakdown;
        this.recommendationReasons = recommendationReasons;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getProfileCompletenessScore() {
        return profileCompletenessScore;
    }

    public void setProfileCompletenessScore(double profileCompletenessScore) {
        this.profileCompletenessScore = profileCompletenessScore;
    }

    public String getFitLevel() {
        return fitLevel;
    }

    public void setFitLevel(String fitLevel) {
        this.fitLevel = fitLevel;
    }

    public SkillMatchDetails getSkillBreakdown() {
        return skillBreakdown;
    }

    public void setSkillBreakdown(SkillMatchDetails skillBreakdown) {
        this.skillBreakdown = skillBreakdown;
    }

    public ExperienceMatchDetails getExperienceBreakdown() {
        return experienceBreakdown;
    }

    public void setExperienceBreakdown(ExperienceMatchDetails experienceBreakdown) {
        this.experienceBreakdown = experienceBreakdown;
    }

    public List<String> getRecommendationReasons() {
        return recommendationReasons;
    }

    public void setRecommendationReasons(List<String> recommendationReasons) {
        this.recommendationReasons = recommendationReasons;
    }
}
