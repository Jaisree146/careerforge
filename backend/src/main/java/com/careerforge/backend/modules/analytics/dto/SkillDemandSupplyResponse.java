package com.careerforge.backend.modules.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class SkillDemandSupplyResponse {

    private List<SkillMetricDto> topDemandedSkills = new ArrayList<>();
    private List<SkillMetricDto> topCandidateSkills = new ArrayList<>();

    public SkillDemandSupplyResponse() {
    }

    public SkillDemandSupplyResponse(List<SkillMetricDto> topDemandedSkills, List<SkillMetricDto> topCandidateSkills) {
        this.topDemandedSkills = topDemandedSkills;
        this.topCandidateSkills = topCandidateSkills;
    }

    public List<SkillMetricDto> getTopDemandedSkills() {
        return topDemandedSkills;
    }

    public void setTopDemandedSkills(List<SkillMetricDto> topDemandedSkills) {
        this.topDemandedSkills = topDemandedSkills;
    }

    public List<SkillMetricDto> getTopCandidateSkills() {
        return topCandidateSkills;
    }

    public void setTopCandidateSkills(List<SkillMetricDto> topCandidateSkills) {
        this.topCandidateSkills = topCandidateSkills;
    }
}
