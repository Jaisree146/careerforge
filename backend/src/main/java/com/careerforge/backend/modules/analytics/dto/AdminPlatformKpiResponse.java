package com.careerforge.backend.modules.analytics.dto;

public class AdminPlatformKpiResponse {

    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalJobs;
    private long publishedJobs;
    private long totalApplications;
    private long totalAuditLogs;

    public AdminPlatformKpiResponse() {
    }

    public AdminPlatformKpiResponse(
            long totalUsers,
            long totalCandidates,
            long totalRecruiters,
            long totalJobs,
            long publishedJobs,
            long totalApplications,
            long totalAuditLogs
    ) {
        this.totalUsers = totalUsers;
        this.totalCandidates = totalCandidates;
        this.totalRecruiters = totalRecruiters;
        this.totalJobs = totalJobs;
        this.publishedJobs = publishedJobs;
        this.totalApplications = totalApplications;
        this.totalAuditLogs = totalAuditLogs;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(long totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public long getTotalRecruiters() {
        return totalRecruiters;
    }

    public void setTotalRecruiters(long totalRecruiters) {
        this.totalRecruiters = totalRecruiters;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getPublishedJobs() {
        return publishedJobs;
    }

    public void setPublishedJobs(long publishedJobs) {
        this.publishedJobs = publishedJobs;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getTotalAuditLogs() {
        return totalAuditLogs;
    }

    public void setTotalAuditLogs(long totalAuditLogs) {
        this.totalAuditLogs = totalAuditLogs;
    }
}
