package com.careerforge.backend.modules.application.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.application.dto.ApplicationApplyRequest;
import com.careerforge.backend.modules.application.dto.ApplicationResponse;
import com.careerforge.backend.modules.application.dto.SavedJobResponse;
import com.careerforge.backend.modules.application.model.ApplicationStatus;
import com.careerforge.backend.modules.application.service.ApplicationService;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Candidate Applications & Saved Jobs", description = "Job applications, tracking lifecycle, and saved/bookmarked jobs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
public class CandidateApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public CandidateApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Apply for a job", description = "Submits an application with candidate's resume and optional cover letter. Prevents duplicate applications.")
    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId,
            @RequestBody(required = false) ApplicationApplyRequest request
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        ApplicationResponse response = applicationService.applyToJob(candidateId, jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job application submitted successfully", response));
    }

    @Operation(summary = "Get candidate applications", description = "Lists all applications submitted by candidate with optional status filter and pagination.")
    @GetMapping("/candidate/applications")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getCandidateApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationResponse> applications = applicationService.getCandidateApplications(candidateId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Candidate applications retrieved", applications));
    }

    @Operation(summary = "Get candidate application by ID", description = "Retrieves full application details and status timeline.")
    @GetMapping("/candidate/applications/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        ApplicationResponse response = applicationService.getCandidateApplicationById(candidateId, id);
        return ResponseEntity.ok(ApiResponse.success("Application details retrieved", response));
    }

    @Operation(summary = "Bookmark a job", description = "Saves a job posting to candidate's saved jobs list.")
    @PostMapping("/jobs/{jobId}/save")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> saveJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        applicationService.saveJob(userId, jobId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job bookmarked successfully", null));
    }

    @Operation(summary = "Unbookmark a job", description = "Removes a job posting from candidate's saved jobs list.")
    @DeleteMapping("/jobs/{jobId}/save")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        applicationService.unsaveJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success("Job removed from bookmarks", null));
    }

    @Operation(summary = "List candidate saved jobs", description = "Lists all bookmarked jobs with pagination.")
    @GetMapping("/candidate/saved-jobs")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<SavedJobResponse>>> getSavedJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SavedJobResponse> savedJobs = applicationService.getSavedJobs(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Saved jobs retrieved", savedJobs));
    }

    @Operation(summary = "Check if job is saved", description = "Checks whether a specific job is bookmarked by the candidate.")
    @GetMapping("/jobs/{jobId}/is-saved")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isJobSaved(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        boolean saved = applicationService.isJobSaved(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success("Bookmark status checked", Map.of("saved", saved)));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
