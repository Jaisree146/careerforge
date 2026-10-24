package com.careerforge.backend.modules.analytics.dto;

public class JobFunnelSummaryDto {

    private Long jobId;
    private String jobTitle;
    private String status;
    private long totalApplications;
    private long appliedCount;
    private long reviewingCount;
    private long shortlistedCount;
    private long interviewingCount;
    private long acceptedCount;
    private long rejectedCount;

    public JobFunnelSummaryDto() {
    }

    public JobFunnelSummaryDto(
            Long jobId,
            String jobTitle,
            String status,
            long totalApplications,
            long appliedCount,
            long reviewingCount,
            long shortlistedCount,
            long interviewingCount,
            long acceptedCount,
            long rejectedCount
    ) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.status = status;
        this.totalApplications = totalApplications;
        this.appliedCount = appliedCount;
        this.reviewingCount = reviewingCount;
        this.shortlistedCount = shortlistedCount;
        this.interviewingCount = interviewingCount;
        this.acceptedCount = acceptedCount;
        this.rejectedCount = rejectedCount;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getAppliedCount() {
        return appliedCount;
    }

    public void setAppliedCount(long appliedCount) {
        this.appliedCount = appliedCount;
    }

    public long getReviewingCount() {
        return reviewingCount;
    }

    public void setReviewingCount(long reviewingCount) {
        this.reviewingCount = reviewingCount;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getInterviewingCount() {
        return interviewingCount;
    }

    public void setInterviewingCount(long interviewingCount) {
        this.interviewingCount = interviewingCount;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
