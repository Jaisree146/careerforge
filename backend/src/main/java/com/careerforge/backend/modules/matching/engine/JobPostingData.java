package com.careerforge.backend.modules.matching.engine;

import java.util.Set;

public class JobPostingData {

    private Long jobId;
    private String title;
    private String requiredExperienceText;
    private double requiredExperienceYears;
    private Set<String> requiredSkillNames;
    private Set<String> optionalSkillNames;

    public JobPostingData(Long jobId, String title, String requiredExperienceText, double requiredExperienceYears, Set<String> requiredSkillNames, Set<String> optionalSkillNames) {
        this.jobId = jobId;
        this.title = title;
        this.requiredExperienceText = requiredExperienceText;
        this.requiredExperienceYears = requiredExperienceYears;
        this.requiredSkillNames = requiredSkillNames;
        this.optionalSkillNames = optionalSkillNames;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public String getRequiredExperienceText() {
        return requiredExperienceText;
    }

    public double getRequiredExperienceYears() {
        return requiredExperienceYears;
    }

    public Set<String> getRequiredSkillNames() {
        return requiredSkillNames;
    }

    public Set<String> getOptionalSkillNames() {
        return optionalSkillNames;
    }
}
