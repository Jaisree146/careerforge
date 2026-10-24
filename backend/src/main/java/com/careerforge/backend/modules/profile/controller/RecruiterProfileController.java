package com.careerforge.backend.modules.profile.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.profile.dto.RecruiterProfileRequest;
import com.careerforge.backend.modules.profile.dto.RecruiterProfileResponse;
import com.careerforge.backend.modules.profile.service.RecruiterProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recruiter Profiles", description = "Recruiter profile management and company association")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/recruiter")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;
    private final UserRepository userRepository;

    public RecruiterProfileController(RecruiterProfileService recruiterProfileService, UserRepository userRepository) {
        this.recruiterProfileService = recruiterProfileService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get recruiter profile", description = "Retrieves current authenticated recruiter's company, department, phone, and verification status.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getAuthenticatedUserId(userDetails);
        RecruiterProfileResponse response = recruiterProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Recruiter profile retrieved", response));
    }

    @Operation(summary = "Update recruiter profile", description = "Updates recruiter company link, phone number, and department name.")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecruiterProfileRequest request
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        RecruiterProfileResponse response = recruiterProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Recruiter profile updated", response));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found"));
        return user.getId();
    }
}
