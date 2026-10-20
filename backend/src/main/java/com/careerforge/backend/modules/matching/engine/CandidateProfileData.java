package com.careerforge.backend.modules.matching.engine;

import java.util.Set;

public class CandidateProfileData {

    private Long candidateId;
    private String name;
    private String email;
    private boolean hasResume;
    private boolean hasBio;
    private boolean hasEducation;
    private double experienceYears;
    private Set<String> skillNames;

    public CandidateProfileData(Long candidateId, String name, String email, boolean hasResume, boolean hasBio, boolean hasEducation, double experienceYears, Set<String> skillNames) {
        this.candidateId = candidateId;
        this.name = name;
        this.email = email;
        this.hasResume = hasResume;
        this.hasBio = hasBio;
        this.hasEducation = hasEducation;
        this.experienceYears = experienceYears;
        this.skillNames = skillNames;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isHasResume() {
        return hasResume;
    }

    public boolean isHasBio() {
        return hasBio;
    }

    public boolean isHasEducation() {
        return hasEducation;
    }

    public double getExperienceYears() {
        return experienceYears;
    }

    public Set<String> getSkillNames() {
        return skillNames;
    }
}
