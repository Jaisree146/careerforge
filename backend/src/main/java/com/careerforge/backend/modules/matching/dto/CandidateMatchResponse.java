package com.careerforge.backend.modules.matching.dto;

public class CandidateMatchResponse {

    private Long candidateId;
    private String name;
    private String email;
    private String resumeUrl;
    private MatchScoreBreakdown match;

    public CandidateMatchResponse() {
    }

    public CandidateMatchResponse(Long candidateId, String name, String email, String resumeUrl, MatchScoreBreakdown match) {
        this.candidateId = candidateId;
        this.name = name;
        this.email = email;
        this.resumeUrl = resumeUrl;
        this.match = match;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
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

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public MatchScoreBreakdown getMatch() {
        return match;
    }

    public void setMatch(MatchScoreBreakdown match) {
        this.match = match;
    }
}
