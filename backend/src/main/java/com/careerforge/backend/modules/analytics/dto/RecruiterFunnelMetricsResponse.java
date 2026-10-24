package com.careerforge.backend.modules.analytics.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecruiterFunnelMetricsResponse {

    private long totalJobsPosted;
    private long totalActiveJobs;
    private long totalApplicationsReceived;
    private Map<String, Long> statusBreakdown = new HashMap<>();
    private double shortlistRatePercentage;
    private double interviewRatePercentage;
    private double acceptanceRatePercentage;
    private List<JobFunnelSummaryDto> jobSummaries = new ArrayList<>();

    public RecruiterFunnelMetricsResponse() {
    }

    public RecruiterFunnelMetricsResponse(
            long totalJobsPosted,
            long totalActiveJobs,
            long totalApplicationsReceived,
            Map<String, Long> statusBreakdown,
            double shortlistRatePercentage,
            double interviewRatePercentage,
            double acceptanceRatePercentage,
            List<JobFunnelSummaryDto> jobSummaries
    ) {
        this.totalJobsPosted = totalJobsPosted;
        this.totalActiveJobs = totalActiveJobs;
        this.totalApplicationsReceived = totalApplicationsReceived;
        this.statusBreakdown = statusBreakdown;
        this.shortlistRatePercentage = shortlistRatePercentage;
        this.interviewRatePercentage = interviewRatePercentage;
        this.acceptanceRatePercentage = acceptanceRatePercentage;
        this.jobSummaries = jobSummaries;
    }

    public long getTotalJobsPosted() {
        return totalJobsPosted;
    }

    public void setTotalJobsPosted(long totalJobsPosted) {
        this.totalJobsPosted = totalJobsPosted;
    }

    public long getTotalActiveJobs() {
        return totalActiveJobs;
    }

    public void setTotalActiveJobs(long totalActiveJobs) {
        this.totalActiveJobs = totalActiveJobs;
    }

    public long getTotalApplicationsReceived() {
        return totalApplicationsReceived;
    }

    public void setTotalApplicationsReceived(long totalApplicationsReceived) {
        this.totalApplicationsReceived = totalApplicationsReceived;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public double getShortlistRatePercentage() {
        return shortlistRatePercentage;
    }

    public void setShortlistRatePercentage(double shortlistRatePercentage) {
        this.shortlistRatePercentage = shortlistRatePercentage;
    }

    public double getInterviewRatePercentage() {
        return interviewRatePercentage;
    }

    public void setInterviewRatePercentage(double interviewRatePercentage) {
        this.interviewRatePercentage = interviewRatePercentage;
    }

    public double getAcceptanceRatePercentage() {
        return acceptanceRatePercentage;
    }

    public void setAcceptanceRatePercentage(double acceptanceRatePercentage) {
        this.acceptanceRatePercentage = acceptanceRatePercentage;
    }

    public List<JobFunnelSummaryDto> getJobSummaries() {
        return jobSummaries;
    }

    public void setJobSummaries(List<JobFunnelSummaryDto> jobSummaries) {
        this.jobSummaries = jobSummaries;
    }
}
