package com.careerforge.backend.modules.application.dto;

public class ApplicationApplyRequest {

    private Long resumeId;
    private String notes;

    public ApplicationApplyRequest() {
    }

    public ApplicationApplyRequest(Long resumeId, String notes) {
        this.resumeId = resumeId;
        this.notes = notes;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
