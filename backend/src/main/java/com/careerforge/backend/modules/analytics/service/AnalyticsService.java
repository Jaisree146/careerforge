package com.careerforge.backend.modules.analytics.service;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.analytics.dto.AdminPlatformKpiResponse;
import com.careerforge.backend.modules.analytics.dto.CandidateAnalyticsResponse;
import com.careerforge.backend.modules.analytics.dto.JobFunnelSummaryDto;
import com.careerforge.backend.modules.analytics.dto.RecruiterFunnelMetricsResponse;
import com.careerforge.backend.modules.analytics.dto.SkillDemandSupplyResponse;
import com.careerforge.backend.modules.analytics.dto.SkillMetricDto;
import com.careerforge.backend.modules.application.model.ApplicationStatus;
import com.careerforge.backend.modules.application.repository.ApplicationRepository;
import com.careerforge.backend.modules.application.repository.SavedJobRepository;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.model.JobStatus;
import com.careerforge.backend.modules.job.repository.CandidateSkillRepository;
import com.careerforge.backend.modules.job.repository.JobRepository;
import com.careerforge.backend.modules.job.repository.JobSkillRepository;
import com.careerforge.backend.modules.notification.repository.AuditLogRepository;
import com.careerforge.backend.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final AuditLogRepository auditLogRepository;

    public AnalyticsService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            SavedJobRepository savedJobRepository,
            JobSkillRepository jobSkillRepository,
            CandidateSkillRepository candidateSkillRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterFunnelMetricsResponse getRecruiterFunnelMetrics(Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));

        long totalJobs = jobRepository.countByRecruiterId(recruiterId);
        long activeJobs = jobRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.PUBLISHED);

        List<Object[]> statusCounts = applicationRepository.countApplicationsByStatusForRecruiter(recruiterId);
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusBreakdown.put(status.name(), 0L);
        }

        long totalApps = 0;
        for (Object[] row : statusCounts) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = ((Number) row[1]).longValue();
            statusBreakdown.put(status.name(), count);
            totalApps += count;
        }

        long shortlisted = statusBreakdown.getOrDefault(ApplicationStatus.SHORTLISTED.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.INTERVIEWING.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.ACCEPTED.name(), 0L);

        long interviewing = statusBreakdown.getOrDefault(ApplicationStatus.INTERVIEWING.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.ACCEPTED.name(), 0L);

        long accepted = statusBreakdown.getOrDefault(ApplicationStatus.ACCEPTED.name(), 0L);

        double shortlistRate = totalApps > 0 ? round(((double) shortlisted / totalApps) * 100.0) : 0.0;
        double interviewRate = shortlisted > 0 ? round(((double) interviewing / shortlisted) * 100.0) : 0.0;
        double acceptanceRate = interviewing > 0 ? round(((double) accepted / interviewing) * 100.0) : 0.0;

        List<Object[]> perJobRows = applicationRepository.countApplicationsPerJobAndStatusForRecruiter(recruiterId);
        Map<Long, JobFunnelSummaryDto> jobMap = new LinkedHashMap<>();

        for (Object[] row : perJobRows) {
            Long jobId = ((Number) row[0]).longValue();
            String title = (String) row[1];
            JobStatus jobStatus = (JobStatus) row[2];
            ApplicationStatus appStatus = (ApplicationStatus) row[3];
            long count = ((Number) row[4]).longValue();

            JobFunnelSummaryDto dto = jobMap.computeIfAbsent(jobId, id -> new JobFunnelSummaryDto(
                    id, title, jobStatus.name(), 0, 0, 0, 0, 0, 0, 0
            ));

            dto.setTotalApplications(dto.getTotalApplications() + count);
            if (appStatus == ApplicationStatus.APPLIED) dto.setAppliedCount(count);
            else if (appStatus == ApplicationStatus.REVIEWING) dto.setReviewingCount(count);
            else if (appStatus == ApplicationStatus.SHORTLISTED) dto.setShortlistedCount(count);
            else if (appStatus == ApplicationStatus.INTERVIEWING) dto.setInterviewingCount(count);
            else if (appStatus == ApplicationStatus.ACCEPTED) dto.setAcceptedCount(count);
            else if (appStatus == ApplicationStatus.REJECTED) dto.setRejectedCount(count);
        }

        return new RecruiterFunnelMetricsResponse(
                totalJobs,
                activeJobs,
                totalApps,
                statusBreakdown,
                shortlistRate,
                interviewRate,
                acceptanceRate,
                new ArrayList<>(jobMap.values())
        );
    }

    @Cacheable(value = CacheConfig.CACHE_SKILL_DEMAND_SUPPLY, key = "#limit")
    @Transactional(readOnly = true)
    public SkillDemandSupplyResponse getSkillDemandSupply(int limit) {
        int pageLimit = limit > 0 ? limit : 10;
        List<Object[]> demandRows = jobSkillRepository.findTopDemandedSkills(PageRequest.of(0, pageLimit));
        List<Object[]> supplyRows = candidateSkillRepository.findTopCandidateSkills(PageRequest.of(0, pageLimit));

        List<SkillMetricDto> demand = new ArrayList<>();
        for (Object[] row : demandRows) {
            demand.add(new SkillMetricDto((String) row[0], ((Number) row[1]).longValue()));
        }

        List<SkillMetricDto> supply = new ArrayList<>();
        for (Object[] row : supplyRows) {
            supply.add(new SkillMetricDto((String) row[0], ((Number) row[1]).longValue()));
        }

        return new SkillDemandSupplyResponse(demand, supply);
    }

    @Transactional(readOnly = true)
    public CandidateAnalyticsResponse getCandidateAnalytics(Long candidateId) {
        userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        List<Object[]> statusCounts = applicationRepository.countApplicationsByStatusForCandidate(candidateId);
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusBreakdown.put(status.name(), 0L);
        }

        long totalApps = 0;
        for (Object[] row : statusCounts) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            statusBreakdown.put(status.name(), count);
            totalApps += count;
        }

        long activeApps = statusBreakdown.getOrDefault(ApplicationStatus.APPLIED.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.REVIEWING.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.SHORTLISTED.name(), 0L)
                + statusBreakdown.getOrDefault(ApplicationStatus.INTERVIEWING.name(), 0L);

        long interviews = statusBreakdown.getOrDefault(ApplicationStatus.INTERVIEWING.name(), 0L);
        long offers = statusBreakdown.getOrDefault(ApplicationStatus.ACCEPTED.name(), 0L);
        long savedJobs = savedJobRepository.countByIdUserId(candidateId);

        return new CandidateAnalyticsResponse(
                totalApps,
                activeApps,
                interviews,
                offers,
                savedJobs,
                statusBreakdown
        );
    }

    @Cacheable(value = CacheConfig.CACHE_PLATFORM_KPIS)
    @Transactional(readOnly = true)
    public AdminPlatformKpiResponse getPlatformKpis() {
        long totalUsers = userRepository.count();
        long totalCandidates = userRepository.countByRole(Role.CANDIDATE);
        long totalRecruiters = userRepository.countByRole(Role.RECRUITER);
        long totalJobs = jobRepository.count();
        long publishedJobs = jobRepository.countByStatus(JobStatus.PUBLISHED);
        long totalApps = applicationRepository.count();
        long totalAudit = auditLogRepository.count();

        return new AdminPlatformKpiResponse(
                totalUsers,
                totalCandidates,
                totalRecruiters,
                totalJobs,
                publishedJobs,
                totalApps,
                totalAudit
        );
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
