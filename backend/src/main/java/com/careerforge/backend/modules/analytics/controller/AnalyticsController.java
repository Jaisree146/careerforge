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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recruitment Analytics", description = "Recruiter hiring funnels, candidate career KPIs, market skill demand/supply, and platform metrics")
@RestController
@RequestMapping("/api/v1")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get recruiter hiring funnel metrics", description = "Calculates total applicants, shortlist rate, interview rate, and offer acceptance rate across recruiter jobs.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recruiter/analytics/funnel")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RecruiterFunnelMetricsResponse>> getRecruiterFunnel(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        RecruiterFunnelMetricsResponse response = analyticsService.getRecruiterFunnelMetrics(recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Recruiter funnel metrics retrieved", response));
    }

    @Operation(summary = "Get market skill demand vs supply", description = "Compares frequency of skills required by active jobs against skills declared by candidates.")
    @GetMapping("/analytics/skills-demand-supply")
    public ResponseEntity<ApiResponse<SkillDemandSupplyResponse>> getSkillDemandSupply(
            @RequestParam(defaultValue = "10") int limit
    ) {
        SkillDemandSupplyResponse response = analyticsService.getSkillDemandSupply(limit);
        return ResponseEntity.ok(ApiResponse.success("Skill demand and supply analytics retrieved", response));
    }

    @Operation(summary = "Get candidate career analytics dashboard", description = "Calculates candidate application distribution by status, interview rate, and top missing skills compared against jobs.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/candidate/analytics/dashboard")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<CandidateAnalyticsResponse>> getCandidateDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        CandidateAnalyticsResponse response = analyticsService.getCandidateAnalytics(candidateId);
        return ResponseEntity.ok(ApiResponse.success("Candidate career dashboard metrics retrieved", response));
    }

    @Operation(summary = "Get administrator platform KPIs", description = "Aggregates global platform numbers: users by role, jobs by status, total applications, and average applications per job.", security = @SecurityRequirement(name = "bearerAuth"))
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
