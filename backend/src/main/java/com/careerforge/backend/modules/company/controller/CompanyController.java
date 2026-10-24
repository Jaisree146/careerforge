package com.careerforge.backend.modules.company.controller;

import com.careerforge.backend.common.response.ApiResponse;
import com.careerforge.backend.modules.company.dto.CompanyRequest;
import com.careerforge.backend.modules.company.dto.CompanyResponse;
import com.careerforge.backend.modules.company.dto.LocationDto;
import com.careerforge.backend.modules.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Companies & Locations", description = "Company directories, hiring organizations, and geographic locations")
@RestController
@RequestMapping("/api/v1")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "Create geographic location", description = "Creates a new location (city, state, country) for jobs and companies.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LocationDto>> createLocation(@Valid @RequestBody LocationDto dto) {
        LocationDto created = companyService.createLocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location created successfully", created));
    }

    @Operation(summary = "Get all locations", description = "Retrieves all supported locations.")
    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAllLocations() {
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", companyService.getAllLocations()));
    }

    @Operation(summary = "Register company profile", description = "Registers a new hiring company with industry, website, logo URL, and location.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/companies")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created successfully", response));
    }

    @Operation(summary = "Update company profile", description = "Modifies existing company details.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/companies/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request
    ) {
        CompanyResponse response = companyService.updateCompany(id, request);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", response));
    }

    @Operation(summary = "Get company by ID", description = "Retrieves full public company profile details.")
    @GetMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Company retrieved successfully", companyService.getCompanyById(id)));
    }

    @Operation(summary = "List all companies", description = "Retrieves directory of all registered companies.")
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies() {
        return ResponseEntity.ok(ApiResponse.success("Companies retrieved successfully", companyService.getAllCompanies()));
    }

    @Operation(summary = "Search companies by name or industry", description = "Searches companies matching name or industry query keyword.")
    @GetMapping("/companies/search")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> searchCompanies(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success("Companies retrieved successfully", companyService.searchCompanies(query)));
    }
}
