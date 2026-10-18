package com.careerforge.backend.modules.profile.dto;

import java.time.Instant;
import java.util.List;

public class CandidateProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private String bio;
    private ResumeDto resume;
    private List<EducationDto> educations;
    private List<ExperienceDto> experiences;
    private Instant createdAt;
    private Instant updatedAt;

    public CandidateProfileResponse() {
    }

    public CandidateProfileResponse(Long userId, String name, String email, String bio, ResumeDto resume, List<EducationDto> educations, List<ExperienceDto> experiences, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.resume = resume;
        this.educations = educations;
        this.experiences = experiences;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ResumeDto getResume() {
        return resume;
    }

    public void setResume(ResumeDto resume) {
        this.resume = resume;
    }

    public List<EducationDto> getEducations() {
        return educations;
    }

    public void setEducations(List<EducationDto> educations) {
        this.educations = educations;
    }

    public List<ExperienceDto> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<ExperienceDto> experiences) {
        this.experiences = experiences;
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
