package com.careerforge.backend.modules.job.dto;

import com.careerforge.backend.modules.job.model.EmploymentType;
import com.careerforge.backend.modules.job.model.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @Size(max = 100, message = "Experience must not exceed 100 characters")
    private String experience;

    @Size(max = 100, message = "Salary must not exceed 100 characters")
    private String salary;

    private JobStatus status = JobStatus.DRAFT;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    private LocalDate deadline;

    private Long companyId;

    private Long locationId;

    private List<JobSkillDto> skills = new ArrayList<>();

    public JobRequest() {
    }

    public JobRequest(String title, String description, String experience, String salary, JobStatus status, EmploymentType employmentType, LocalDate deadline, Long companyId, Long locationId, List<JobSkillDto> skills) {
        this.title = title;
        this.description = description;
        this.experience = experience;
        this.salary = salary;
        this.status = status;
        this.employmentType = employmentType;
        this.deadline = deadline;
        this.companyId = companyId;
        this.locationId = locationId;
        this.skills = skills;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public List<JobSkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<JobSkillDto> skills) {
        this.skills = skills;
    }
}
