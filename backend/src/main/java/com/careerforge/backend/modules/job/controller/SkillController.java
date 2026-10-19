package com.careerforge.backend.modules.job.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.dto.SkillDto;
import com.careerforge.backend.modules.job.service.SkillService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SkillController {

    private final SkillService skillService;
    private final UserRepository userRepository;

    public SkillController(SkillService skillService, UserRepository userRepository) {
        this.skillService = skillService;
        this.userRepository = userRepository;
    }

    @PostMapping("/skills")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SkillDto>> createSkill(@Valid @RequestBody SkillDto dto) {
        SkillDto created = skillService.createSkill(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created successfully", created));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getAllSkills() {
        return ResponseEntity.ok(ApiResponse.success("Skills retrieved successfully", skillService.getAllSkills()));
    }

    @GetMapping("/candidate/skills")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getCandidateSkills(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getAuthenticatedUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Candidate skills retrieved", skillService.getCandidateSkills(userId)));
    }

    @PostMapping("/candidate/skills/{skillId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<SkillDto>> addCandidateSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long skillId
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        SkillDto added = skillService.addCandidateSkill(userId, skillId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill added to profile", added));
    }

    @DeleteMapping("/candidate/skills/{skillId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCandidateSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long skillId
    ) {
        Long userId = getAuthenticatedUserId(userDetails);
        skillService.removeCandidateSkill(userId, skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill removed from profile", null));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found"));
        return user.getId();
    }
}
