package com.careerforge.backend.modules.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SkillDto {

    private Long id;

    @NotBlank(message = "Skill name is required")
    @Size(min = 1, max = 100, message = "Skill name must not exceed 100 characters")
    private String name;

    public SkillDto() {
    }

    public SkillDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
