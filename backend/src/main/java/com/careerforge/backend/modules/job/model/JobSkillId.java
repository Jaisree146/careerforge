package com.careerforge.backend.modules.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JobSkillId implements Serializable {

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "skill_id")
    private Long skillId;

    public JobSkillId() {
    }

    public JobSkillId(Long jobId, Long skillId) {
        this.jobId = jobId;
        this.skillId = skillId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSkillId that = (JobSkillId) o;
        return Objects.equals(jobId, that.jobId) && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, skillId);
    }
}
