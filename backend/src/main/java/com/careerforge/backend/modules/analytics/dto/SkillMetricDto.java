package com.careerforge.backend.modules.analytics.dto;

public class SkillMetricDto {

    private String skillName;
    private long count;

    public SkillMetricDto() {
    }

    public SkillMetricDto(String skillName, long count) {
        this.skillName = skillName;
        this.count = count;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
