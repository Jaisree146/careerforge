package com.careerforge.backend.modules.profile.dto;

import jakarta.validation.constraints.Size;

public class CandidateProfileRequest {

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    public CandidateProfileRequest() {
    }

    public CandidateProfileRequest(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
