package com.careerforge.backend.modules.storage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Service contract for persistent file storage, path security, and document parsing.
 */
public interface FileStorageService {

    /**
     * Stores a candidate resume file with path traversal defense,
     * MIME validation, and secure UUID filename generation.
     */
    StoredFile storeResumeFile(MultipartFile file, Long userId);

    /**
     * Loads a stored file as a Spring Resource for HTTP streaming downloads.
     */
    Resource loadFileAsResource(String fileName);

    /**
     * Deletes a stored file from disk.
     */
    void deleteFile(String fileName);

    /**
     * Extracts text content from a PDF document using Apache PDFBox.
     */
    String extractTextFromPdf(Path filePath);

    /**
     * Metadata record returned when a file is stored.
     */
    record StoredFile(
            String storedFileName,
            String originalFileName,
            String contentType,
            long sizeBytes,
            Path filePath,
            String extractedText
    ) {}
}
