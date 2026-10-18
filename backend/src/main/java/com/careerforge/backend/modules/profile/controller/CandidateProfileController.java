package com.careerforge.backend.modules.profile.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.profile.dto.CandidateProfileRequest;
import com.careerforge.backend.modules.profile.dto.CandidateProfileResponse;
import com.careerforge.backend.modules.profile.dto.EducationDto;
import com.careerforge.backend.modules.profile.dto.ExperienceDto;
import com.careerforge.backend.modules.profile.dto.ResumeDto;
import com.careerforge.backend.modules.profile.service.CandidateProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/candidate")
@PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
public class CandidateProfileController {

    private final CandidateProfileService profileService;
    private final UserRepository userRepository;

    public CandidateProfileController(CandidateProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getAuthenticatedUserId(userDetails);
        CandidateProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Candidate profile retrieved", response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateBio(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CandidateProfileRequest request
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        CandidateProfileResponse response = profileService.updateBio(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Candidate profile updated", response));
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<EducationDto>> addEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EducationDto dto
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        EducationDto response = profileService.addEducation(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Education record added", response));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        profileService.deleteEducation(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Education record deleted", null));
    }

    @PostMapping("/experience")
    public ResponseEntity<ApiResponse<ExperienceDto>> addExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExperienceDto dto
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        ExperienceDto response = profileService.addExperience(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Experience record added", response));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        profileService.deleteExperience(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Experience record deleted", null));
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<ResumeDto>> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ResumeDto dto
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        ResumeDto response = profileService.uploadOrUpdateResume(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Resume metadata saved successfully", response));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found"));
        return user.getId();
    }
}
