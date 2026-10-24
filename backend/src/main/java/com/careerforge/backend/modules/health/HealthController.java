package com.careerforge.backend.modules.health;

import com.careerforge.backend.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "System Health", description = "Application uptime probe and MySQL connection health verification")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Diagnostic health-check endpoint.
     * Verifies that the Spring context is active, HikariCP pool can acquire a connection,
     * and queries live MySQL server metadata.
     */
    @Operation(summary = "System health check", description = "Probes backend service availability and verifies live MySQL database connection pool status.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthDetails = new LinkedHashMap<>();
        healthDetails.put("status", "UP");
        healthDetails.put("service", "careerforge-backend");

        Map<String, Object> dbDetails = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            dbDetails.put("status", "CONNECTED");
            dbDetails.put("databaseProduct", metaData.getDatabaseProductName());
            dbDetails.put("databaseVersion", metaData.getDatabaseProductVersion());
            dbDetails.put("catalog", connection.getCatalog());
            dbDetails.put("driverName", metaData.getDriverName());
            healthDetails.put("database", dbDetails);

            return ResponseEntity.ok(ApiResponse.success("CareerForge Backend is operational and connected to MySQL", healthDetails));
        } catch (Exception ex) {
            dbDetails.put("status", "DISCONNECTED");
            dbDetails.put("error", ex.getMessage());
            healthDetails.put("status", "DEGRADED");
            healthDetails.put("database", dbDetails);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.failure("Database connection failed: " + ex.getMessage()));
        }
    }
}
