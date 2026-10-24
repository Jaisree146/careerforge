package com.careerforge.backend.modules.job.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.dto.JobRequest;
import com.careerforge.backend.modules.job.dto.JobResponse;
import com.careerforge.backend.modules.job.dto.JobStatusUpdateRequest;
import com.careerforge.backend.modules.job.model.EmploymentType;
import com.careerforge.backend.modules.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Jobs", description = "Job posting lifecycle management, search, and filtering")
@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    public JobController(JobService jobService, UserRepository userRepository) {
        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Create job posting", description = "Creates a new job posting with company, location, experience range, salary, and required skills.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobRequest request
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        JobResponse response = jobService.createJob(recruiterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posting created successfully", response));
    }

    @Operation(summary = "Update job posting", description = "Updates job details, skills, location, and salary info.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        JobResponse response = jobService.updateJob(recruiterId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Job posting updated successfully", response));
    }

    @Operation(summary = "Update job status", description = "Transitions job status (e.g. DRAFT -> PUBLISHED -> CLOSED).", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/jobs/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJobStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody JobStatusUpdateRequest request
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        JobResponse response = jobService.updateJobStatus(recruiterId, id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Job status updated successfully", response));
    }

    @Operation(summary = "Get jobs posted by current recruiter", description = "Lists all jobs posted by the authenticated recruiter.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recruiter/jobs")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getRecruiterJobs(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        List<JobResponse> jobs = jobService.getRecruiterJobs(recruiterId);
        return ResponseEntity.ok(ApiResponse.success("Recruiter jobs retrieved", jobs));
    }

    @Operation(summary = "Get job posting by ID", description = "Retrieves public details of a single job posting.")
    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", response));
    }

    @Operation(summary = "Search published jobs", description = "Search and filter open jobs with pagination by title, location, company, and employment type.")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JobResponse> result = jobService.searchPublishedJobs(title, locationId, companyId, employmentType, pageable);
        return ResponseEntity.ok(ApiResponse.success("Published jobs retrieved", result));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found"));
        return user.getId();
    }
}
