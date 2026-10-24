package com.careerforge.backend.modules.application.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.application.dto.ApplicationResponse;
import com.careerforge.backend.modules.application.dto.ApplicationStatusUpdateRequest;
import com.careerforge.backend.modules.application.model.ApplicationStatus;
import com.careerforge.backend.modules.application.service.ApplicationService;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recruiter Applicant Tracking", description = "Applicant tracking pipeline, candidate review, and stage progression")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/recruiter")
public class RecruiterApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public RecruiterApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get applicants for a job", description = "Retrieves paginated applications for a specific recruiter-owned job posting, filterable by stage.")
    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getJobApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationResponse> applications = applicationService.getJobApplicationsForRecruiter(recruiterId, jobId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Job applications retrieved", applications));
    }

    @Operation(summary = "Get all recruiter applicants across jobs", description = "Retrieves all applicants across all jobs posted by the recruiter.")
    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAllApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationResponse> applications = applicationService.getAllApplicationsForRecruiter(recruiterId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("All recruiter applications retrieved", applications));
    }

    @Operation(summary = "Update applicant status", description = "Progresses candidate through pipeline stages (e.g. SUBMITTED -> SHORTLISTED -> INTERVIEW_SCHEDULED -> OFFERED/REJECTED). Emits notification events.")
    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        ApplicationResponse response = applicationService.updateApplicationStatus(recruiterId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", response));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated recruiter not found"));
        return user.getId();
    }
}
