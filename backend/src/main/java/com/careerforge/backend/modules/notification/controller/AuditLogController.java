package com.careerforge.backend.modules.notification.controller;

import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.notification.dto.AuditLogResponse;
import com.careerforge.backend.modules.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Audit Logs", description = "System-wide administrative security audit trail and event logging")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final NotificationService notificationService;

    public AuditLogController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get system audit logs", description = "Retrieves immutable administrative audit trail logs with actor, action, and target entity metadata.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> logs = notificationService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success("System audit logs retrieved successfully", logs));
    }
}

