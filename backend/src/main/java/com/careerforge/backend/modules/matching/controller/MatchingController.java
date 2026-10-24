package com.careerforge.backend.modules.matching.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.matching.dto.CandidateMatchResponse;
import com.careerforge.backend.modules.matching.dto.JobMatchResponse;
import com.careerforge.backend.modules.matching.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AI Matching Engine", description = "Multi-factor candidate-job compatibility scoring across skills, experience, and completeness")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
public class MatchingController {

    private final MatchingService matchingService;
    private final UserRepository userRepository;

    public MatchingController(MatchingService matchingService, UserRepository userRepository) {
        this.matchingService = matchingService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get recommended jobs for candidate", description = "Calculates multi-factor compatibility scores across published jobs and returns top matches ranked by match percentage.")
    @GetMapping("/candidate/job-matches")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<JobMatchResponse>>> getCandidateJobMatches(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        List<JobMatchResponse> matches = matchingService.getJobMatchesForCandidate(candidateId, limit);
        return ResponseEntity.ok(ApiResponse.success("Recommended job matches calculated successfully", matches));
    }

    @Operation(summary = "Get detailed match score for a job", description = "Returns deep dimensional breakdown of compatibility (skills 60%, experience 25%, profile completeness 15%) between candidate and job.")
    @GetMapping("/jobs/{jobId}/match")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobMatchResponse>> getJobMatchDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        JobMatchResponse match = matchingService.getJobMatchDetails(candidateId, jobId);
        return ResponseEntity.ok(ApiResponse.success("Job match score retrieved successfully", match));
    }

    @Operation(summary = "Rank candidate applicants for a job", description = "Scores and ranks all candidates against job requirements for recruiter review.")
    @GetMapping("/recruiter/jobs/{jobId}/candidate-matches")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CandidateMatchResponse>>> getCandidateMatchesForJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        List<CandidateMatchResponse> matches = matchingService.getCandidateMatchesForJob(recruiterId, jobId, limit);
        return ResponseEntity.ok(ApiResponse.success("Ranked candidate matches calculated successfully", matches));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
