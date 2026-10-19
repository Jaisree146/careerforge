package com.careerforge.backend.modules.job.dto;

import jakarta.validation.constraints.NotNull;

public class JobSkillDto {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    private String skillName;

    private boolean required = true;

    public JobSkillDto() {
    }

    public JobSkillDto(Long skillId, String skillName, boolean required) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.required = required;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
