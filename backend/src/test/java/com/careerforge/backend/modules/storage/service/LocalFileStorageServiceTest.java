package com.careerforge.backend.modules.storage.service;

import com.careerforge.backend.common.exception.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Local File Storage & PDFBox Text Extraction Service Unit Tests")
class LocalFileStorageServiceTest {

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        storageService = new LocalFileStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    @DisplayName("Should successfully store valid PDF resume and extract text using Apache PDFBox")
    void shouldStoreAndExtractPdfText() throws IOException {
        // Arrange: Generate a simple valid PDF in memory
        byte[] pdfBytes = createSamplePdf("John Doe - Senior Software Engineer. Proficient in Java, Spring Boot, MySQL.");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                pdfBytes
        );

        // Act
        FileStorageService.StoredFile storedFile = storageService.storeResumeFile(multipartFile, 1L);

        // Assert
        assertThat(storedFile).isNotNull();
        assertThat(storedFile.storedFileName()).isNotBlank().endsWith(".pdf");
        assertThat(storedFile.extractedText()).contains("John Doe").contains("Java").contains("Spring Boot");
    }

    @Test
    @DisplayName("Should reject unauthorized file extensions with BadRequestException")
    void shouldRejectUnauthorizedFileExtension() {
        MockMultipartFile executableFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "echo Exploit".getBytes()
        );

        assertThatThrownBy(() -> storageService.storeResumeFile(executableFile, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("Should reject empty files with BadRequestException")
    void shouldRejectEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> storageService.storeResumeFile(emptyFile, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Failed to store empty file");
    }

    @Test
    @DisplayName("Should reject files exceeding 5MB limit with BadRequestException")
    void shouldRejectOversizedFile() {
        byte[] oversizedBytes = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large_portfolio.pdf",
                "application/pdf",
                oversizedBytes
        );

        assertThatThrownBy(() -> storageService.storeResumeFile(largeFile, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds maximum permitted limit");
    }

    private byte[] createSamplePdf(String content) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(content);
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
