package com.careerforge.backend.modules.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_skills")
public class JobSkill {

    @EmbeddedId
    private JobSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    public JobSkill() {
    }

    public JobSkill(Job job, Skill skill, boolean required) {
        this.job = job;
        this.skill = skill;
        this.required = required;
        this.id = new JobSkillId(job != null ? job.getId() : null, skill != null ? skill.getId() : null);
    }

    public JobSkillId getId() {
        return id;
    }

    public void setId(JobSkillId id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
        if (job != null && this.skill != null) {
            this.id = new JobSkillId(job.getId(), this.skill.getId());
        }
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
        if (this.job != null && skill != null) {
            this.id = new JobSkillId(this.job.getId(), skill.getId());
        }
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
