package com.careerforge.backend.modules.storage.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Local filesystem storage provider for candidate resumes.
 * Incorporates path traversal prevention, MIME-type and size verification,
 * and automated Apache PDFBox text extraction.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "doc", "docx");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/octet-stream"
    );

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.storage.upload-dir:uploads/resumes}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Resume storage directory initialized at: {}", this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize resume storage directory: " + this.rootLocation, e);
        }
    }

    @Override
    public StoredFile storeResumeFile(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum permitted limit of 5MB");
        }

        String rawOriginalFilename = file.getOriginalFilename();
        if (rawOriginalFilename == null || rawOriginalFilename.isBlank()) {
            throw new BadRequestException("File must have a valid filename");
        }

        String cleanedFilename = StringUtils.cleanPath(rawOriginalFilename);
        if (cleanedFilename.contains("..")) {
            throw new BadRequestException("Cannot store file with relative path sequence: " + cleanedFilename);
        }

        // Validate extension
        String extension = getFileExtension(cleanedFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file format ." + extension + ". Allowed formats: " + ALLOWED_EXTENSIONS);
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported media type: " + contentType + ". Only PDF and Word documents are permitted.");
        }

        // Generate unique, collision-resistant filename
        String storedFileName = "resume_user" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
        Path destinationFile = this.rootLocation.resolve(storedFileName).normalize().toAbsolutePath();

        // Enforce boundary defense against directory traversal
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new BadRequestException("Security exception: Cannot store file outside target directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored resume file for userId {}: {}", userId, storedFileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist file to disk: " + storedFileName, e);
        }

        // Extract text if PDF
        String extractedText = "";
        if ("pdf".equalsIgnoreCase(extension)) {
            extractedText = extractTextFromPdf(destinationFile);
        }

        return new StoredFile(
                storedFileName,
                cleanedFilename,
                contentType != null ? contentType : "application/octet-stream",
                file.getSize(),
                destinationFile,
                extractedText
        );
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            String cleanName = StringUtils.cleanPath(fileName);
            if (cleanName.contains("..")) {
                throw new BadRequestException("Invalid file path request");
            }

            Path filePath = this.rootLocation.resolve(cleanName).normalize();
            if (!filePath.getParent().equals(this.rootLocation)) {
                throw new BadRequestException("Access denied: invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file not found on disk: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Invalid file URL: " + fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            if (filePath.getParent().equals(this.rootLocation)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted resume file: {}", fileName);
            }
        } catch (IOException e) {
            log.warn("Failed to delete resume file: {}", fileName, e);
        }
    }

    @Override
    public String extractTextFromPdf(Path filePath) {
        File pdfFile = filePath.toFile();
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            log.warn("Cannot extract text from non-existent or unreadable file: {}", filePath);
            return "";
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Successfully extracted {} characters from PDF resume: {}", text.length(), filePath.getFileName());
            return text != null ? text.trim() : "";
        } catch (Exception ex) {
            log.warn("Failed to extract text from PDF document {}: {}", filePath.getFileName(), ex.getMessage());
            return "";
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
