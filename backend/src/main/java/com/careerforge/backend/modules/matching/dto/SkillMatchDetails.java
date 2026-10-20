package com.careerforge.backend.modules.matching.dto;

import java.util.ArrayList;
import java.util.List;

public class SkillMatchDetails {

    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingRequiredSkills = new ArrayList<>();
    private List<String> missingOptionalSkills = new ArrayList<>();
    private double skillScore;

    public SkillMatchDetails() {
    }

    public SkillMatchDetails(List<String> matchedSkills, List<String> missingRequiredSkills, List<String> missingOptionalSkills, double skillScore) {
        this.matchedSkills = matchedSkills;
        this.missingRequiredSkills = missingRequiredSkills;
        this.missingOptionalSkills = missingOptionalSkills;
        this.skillScore = skillScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingRequiredSkills() {
        return missingRequiredSkills;
    }

    public void setMissingRequiredSkills(List<String> missingRequiredSkills) {
        this.missingRequiredSkills = missingRequiredSkills;
    }

    public List<String> getMissingOptionalSkills() {
        return missingOptionalSkills;
    }

    public void setMissingOptionalSkills(List<String> missingOptionalSkills) {
        this.missingOptionalSkills = missingOptionalSkills;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }
}
