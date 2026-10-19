package com.careerforge.backend.modules.job.dto;

import com.careerforge.backend.modules.company.dto.CompanyResponse;
import com.careerforge.backend.modules.company.dto.LocationDto;
import com.careerforge.backend.modules.job.model.EmploymentType;
import com.careerforge.backend.modules.job.model.JobStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String experience;
    private String salary;
    private JobStatus status;
    private EmploymentType employmentType;
    private LocalDate deadline;
    private CompanyResponse company;
    private Long recruiterId;
    private String recruiterName;
    private LocationDto location;
    private List<JobSkillDto> skills = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public JobResponse() {
    }

    public JobResponse(Long id, String title, String description, String experience, String salary, JobStatus status, EmploymentType employmentType, LocalDate deadline, CompanyResponse company, Long recruiterId, String recruiterName, LocationDto location, List<JobSkillDto> skills, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.experience = experience;
        this.salary = salary;
        this.status = status;
        this.employmentType = employmentType;
        this.deadline = deadline;
        this.company = company;
        this.recruiterId = recruiterId;
        this.recruiterName = recruiterName;
        this.location = location;
        this.skills = skills;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public CompanyResponse getCompany() {
        return company;
    }

    public void setCompany(CompanyResponse company) {
        this.company = company;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public List<JobSkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<JobSkillDto> skills) {
        this.skills = skills;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
