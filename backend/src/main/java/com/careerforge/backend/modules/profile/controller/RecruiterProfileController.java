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

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getAuthenticatedUserId(userDetails);
        RecruiterProfileResponse response = recruiterProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Recruiter profile retrieved", response));
    }

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
