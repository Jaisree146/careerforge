package com.careerforge.backend.modules.storage.controller;

import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisRequest;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisResponse;
import com.careerforge.backend.modules.ai.service.GeminiAiService;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.profile.dto.ResumeDto;
import com.careerforge.backend.modules.profile.service.CandidateProfileService;
import com.careerforge.backend.modules.storage.dto.FileUploadResponse;
import com.careerforge.backend.modules.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;

/**
 * Controller providing multi-part resume document upload,
 * PDF text extraction, integrated Gemini AI parsing, and secure inline file streaming.
 */
@Tag(name = "Resume Storage & Document Processing", description = "Multipart PDF resume upload, PDFBox text extraction, inline viewing, and AI integration")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
public class FileStorageController {

    private final FileStorageService fileStorageService;
    private final CandidateProfileService candidateProfileService;
    private final GeminiAiService geminiAiService;
    private final UserRepository userRepository;

    public FileStorageController(FileStorageService fileStorageService,
                                 CandidateProfileService candidateProfileService,
                                 GeminiAiService geminiAiService,
                                 UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.candidateProfileService = candidateProfileService;
        this.geminiAiService = geminiAiService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Upload candidate PDF resume", description = "Uploads a multipart PDF resume file (max 5MB), validates MIME type, extracts text via PDFBox, updates profile metadata, and optionally triggers immediate Gemini AI analysis.")
    @PostMapping(value = "/candidate/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "analyzeWithAi", defaultValue = "false") boolean analyzeWithAi
    ) {
        Long userId = getAuthenticatedUserId(userDetails);

        // Store file and extract text
        FileStorageService.StoredFile storedFile = fileStorageService.storeResumeFile(file, userId);

        // Build streaming download URI
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/candidate/resume/download/")
                .path(storedFile.storedFileName())
                .toUriString();

        // Update candidate profile resume record in database
        candidateProfileService.uploadOrUpdateResume(userId, new ResumeDto(null, fileDownloadUri, null));

        // Optional immediate Gemini AI analysis
        ResumeAnalysisResponse aiAnalysis = null;
        if (analyzeWithAi && StringUtils.hasText(storedFile.extractedText())) {
            aiAnalysis = geminiAiService.analyzeResume(new ResumeAnalysisRequest(storedFile.extractedText()));
        }

        String preview = storedFile.extractedText();
        if (preview != null && preview.length() > 300) {
            preview = preview.substring(0, 300) + "...";
        }

        FileUploadResponse response = new FileUploadResponse(
                storedFile.storedFileName(),
                storedFile.originalFileName(),
                fileDownloadUri,
                storedFile.sizeBytes(),
                storedFile.contentType(),
                Instant.now(),
                preview,
                aiAnalysis
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded and processed successfully", response));
    }

    @Operation(summary = "Download or stream candidate resume file", description = "Streams stored PDF resume inline for browser preview or recruiter applicant review.")
    @GetMapping("/candidate/resume/download/{fileName:.+}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<Resource> downloadResume(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = "application/pdf";
        if (fileName.toLowerCase().endsWith(".doc")) {
            contentType = "application/msword";
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return user.getId();
    }
}
