package com.careerforge.backend.modules.matching.dto;

public class JobMatchResponse {

    private Long jobId;
    private String title;
    private String companyName;
    private String locationName;
    private String salary;
    private String employmentType;
    private MatchScoreBreakdown match;

    public JobMatchResponse() {
    }

    public JobMatchResponse(Long jobId, String title, String companyName, String locationName, String salary, String employmentType, MatchScoreBreakdown match) {
        this.jobId = jobId;
        this.title = title;
        this.companyName = companyName;
        this.locationName = locationName;
        this.salary = salary;
        this.employmentType = employmentType;
        this.match = match;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public MatchScoreBreakdown getMatch() {
        return match;
    }

    public void setMatch(MatchScoreBreakdown match) {
        this.match = match;
    }
}
