package com.careerforge.backend.modules.analytics.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.analytics.dto.AdminPlatformKpiResponse;
import com.careerforge.backend.modules.analytics.dto.CandidateAnalyticsResponse;
import com.careerforge.backend.modules.analytics.dto.RecruiterFunnelMetricsResponse;
import com.careerforge.backend.modules.analytics.dto.SkillDemandSupplyResponse;
import com.careerforge.backend.modules.analytics.service.AnalyticsService;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/recruiter/analytics/funnel")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RecruiterFunnelMetricsResponse>> getRecruiterFunnel(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        RecruiterFunnelMetricsResponse response = analyticsService.getRecruiterFunnelMetrics(recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Recruiter funnel metrics retrieved", response));
    }

    @GetMapping("/analytics/skills-demand-supply")
    public ResponseEntity<ApiResponse<SkillDemandSupplyResponse>> getSkillDemandSupply(
            @RequestParam(defaultValue = "10") int limit
    ) {
        SkillDemandSupplyResponse response = analyticsService.getSkillDemandSupply(limit);
        return ResponseEntity.ok(ApiResponse.success("Skill demand and supply analytics retrieved", response));
    }

    @GetMapping("/candidate/analytics/dashboard")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<CandidateAnalyticsResponse>> getCandidateDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        CandidateAnalyticsResponse response = analyticsService.getCandidateAnalytics(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate career dashboard metrics retrieved", response));
    }

    @GetMapping("/admin/analytics/platform-kpis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminPlatformKpiResponse>> getPlatformKpis() {
        AdminPlatformKpiResponse response = analyticsService.getPlatformKpis();
        return ResponseEntity.ok(ApiResponse.success("Platform KPIs retrieved successfully", response));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
