package com.careerforge.backend.modules.analytics.dto;

import java.util.HashMap;
import java.util.Map;

public class CandidateAnalyticsResponse {

    private long totalApplications;
    private long activeApplications;
    private long interviewsScheduled;
    private long offersAccepted;
    private long savedJobsCount;
    private Map<String, Long> statusBreakdown = new HashMap<>();

    public CandidateAnalyticsResponse() {
    }

    public CandidateAnalyticsResponse(
            long totalApplications,
            long activeApplications,
            long interviewsScheduled,
            long offersAccepted,
            long savedJobsCount,
            Map<String, Long> statusBreakdown
    ) {
        this.totalApplications = totalApplications;
        this.activeApplications = activeApplications;
        this.interviewsScheduled = interviewsScheduled;
        this.offersAccepted = offersAccepted;
        this.savedJobsCount = savedJobsCount;
        this.statusBreakdown = statusBreakdown;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getActiveApplications() {
        return activeApplications;
    }

    public void setActiveApplications(long activeApplications) {
        this.activeApplications = activeApplications;
    }

    public long getInterviewsScheduled() {
        return interviewsScheduled;
    }

    public void setInterviewsScheduled(long interviewsScheduled) {
        this.interviewsScheduled = interviewsScheduled;
    }

    public long getOffersAccepted() {
        return offersAccepted;
    }

    public void setOffersAccepted(long offersAccepted) {
        this.offersAccepted = offersAccepted;
    }

    public long getSavedJobsCount() {
        return savedJobsCount;
    }

    public void setSavedJobsCount(long savedJobsCount) {
        this.savedJobsCount = savedJobsCount;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }
}
