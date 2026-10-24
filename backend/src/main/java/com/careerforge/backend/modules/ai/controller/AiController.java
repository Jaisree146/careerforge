package com.careerforge.backend.modules.ai.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.ai.dto.ApplicationAdviceResponse;
import com.careerforge.backend.modules.ai.dto.InterviewQuestionsResponse;
import com.careerforge.backend.modules.ai.dto.JobDescriptionGenerateRequest;
import com.careerforge.backend.modules.ai.dto.JobDescriptionGenerateResponse;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisRequest;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisResponse;
import com.careerforge.backend.modules.ai.service.GeminiAiService;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Google Gemini AI", description = "Generative AI capabilities: job description generation, resume skills extraction, candidate interview questions, and tailored cover letters")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final GeminiAiService aiService;
    private final UserRepository userRepository;

    public AiController(GeminiAiService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Generate job description with Gemini AI", description = "Generates a structured, engaging job description with responsibilities, qualifications, and recommended skills based on title and requirements.")
    @PostMapping("/jobs/generate-description")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobDescriptionGenerateResponse>> generateJobDescription(
            @Valid @RequestBody JobDescriptionGenerateRequest request
    ) {
        JobDescriptionGenerateResponse response = aiService.generateJobDescription(request);
        return ResponseEntity.ok(ApiResponse.success("AI job description generated successfully", response));
    }

    @Operation(summary = "Analyze resume text with Gemini AI", description = "Extracts technical skills, soft skills, seniority level, summary, and improvement suggestions from resume text.")
    @PostMapping("/resume/analyze")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ResumeAnalysisResponse>> analyzeResume(
            @Valid @RequestBody ResumeAnalysisRequest request
    ) {
        ResumeAnalysisResponse response = aiService.analyzeResume(request);
        return ResponseEntity.ok(ApiResponse.success("AI resume analysis completed successfully", response));
    }

    @Operation(summary = "Generate tailored interview questions with Gemini AI", description = "Synthesizes candidate profile against job posting to generate tailored behavioral, technical, and scenario-based interview questions.")
    @GetMapping("/recruiter/applications/{applicationId}/interview-questions")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InterviewQuestionsResponse>> generateInterviewQuestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId
    ) {
        Long recruiterId = getAuthenticatedUserId(userDetails);
        InterviewQuestionsResponse response = aiService.generateInterviewQuestions(recruiterId, applicationId);
        return ResponseEntity.ok(ApiResponse.success("AI interview questions generated successfully", response));
    }

    @Operation(summary = "Generate candidate job advice and cover letter draft with Gemini AI", description = "Analyzes fit strengths, missing areas, key interview talking points, and a customized cover letter draft.")
    @GetMapping("/candidate/jobs/{jobId}/advice")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationAdviceResponse>> generateApplicationAdvice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long jobId
    ) {
        Long candidateId = getAuthenticatedUserId(userDetails);
        ApplicationAdviceResponse response = aiService.generateApplicationAdvice(candidateId, jobId);
        return ResponseEntity.ok(ApiResponse.success("AI application advice and cover letter generated successfully", response));
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
