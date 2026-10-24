package com.careerforge.backend.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class ResumeAnalysisRequest {

    @NotBlank(message = "Resume or profile text is required")
    private String resumeText;

    public ResumeAnalysisRequest() {
    }

    public ResumeAnalysisRequest(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
}
