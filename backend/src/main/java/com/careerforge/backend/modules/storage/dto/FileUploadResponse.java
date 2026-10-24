package com.careerforge.backend.modules.storage.dto;

import com.careerforge.backend.modules.ai.dto.ResumeAnalysisResponse;

import java.time.Instant;

/**
 * Encapsulates the result of a multi-part file upload,
 * including stored file identifiers, streaming URL, text extraction,
 * and optional integrated Gemini AI analysis.
 */
public class FileUploadResponse {

    private String fileName;
    private String originalFileName;
    private String fileDownloadUri;
    private long fileSizeBytes;
    private String contentType;
    private Instant uploadedAt;
    private String extractedTextPreview;
    private ResumeAnalysisResponse aiAnalysis;

    public FileUploadResponse() {
    }

    public FileUploadResponse(String fileName, String originalFileName, String fileDownloadUri,
                              long fileSizeBytes, String contentType, Instant uploadedAt,
                              String extractedTextPreview, ResumeAnalysisResponse aiAnalysis) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileSizeBytes = fileSizeBytes;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.extractedTextPreview = extractedTextPreview;
        this.aiAnalysis = aiAnalysis;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileDownloadUri() {
        return fileDownloadUri;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getExtractedTextPreview() {
        return extractedTextPreview;
    }

    public void setExtractedTextPreview(String extractedTextPreview) {
        this.extractedTextPreview = extractedTextPreview;
    }

    public ResumeAnalysisResponse getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(ResumeAnalysisResponse aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}
