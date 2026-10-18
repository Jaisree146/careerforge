package com.careerforge.backend.modules.company.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.company.dto.CompanyRequest;
import com.careerforge.backend.modules.company.dto.CompanyResponse;
import com.careerforge.backend.modules.company.dto.LocationDto;
import com.careerforge.backend.modules.company.model.Company;
import com.careerforge.backend.modules.company.model.Location;
import com.careerforge.backend.modules.company.repository.CompanyRepository;
import com.careerforge.backend.modules.company.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;

    public CompanyService(CompanyRepository companyRepository, LocationRepository locationRepository) {
        this.companyRepository = companyRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public LocationDto createLocation(LocationDto dto) {
        if (locationRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new BadRequestException("Location already exists: " + dto.getName());
        }
        Location location = new Location(dto.getName().trim());
        Location saved = locationRepository.save(location);
        return new LocationDto(saved.getId(), saved.getName());
    }

    @Transactional(readOnly = true)
    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(loc -> new LocationDto(loc.getId(), loc.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + request.getLocationId()));
        }

        Company company = new Company(
                request.getName().trim(),
                request.getDescription(),
                request.getWebsiteUrl(),
                location
        );

        Company saved = companyRepository.save(company);
        return mapToResponse(saved);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + request.getLocationId()));
            company.setLocation(location);
        } else {
            company.setLocation(null);
        }

        company.setName(request.getName().trim());
        company.setDescription(request.getDescription());
        company.setWebsiteUrl(request.getWebsiteUrl());

        Company updated = companyRepository.save(company);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return mapToResponse(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompanies(String query) {
        return companyRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CompanyResponse mapToResponse(Company company) {
        LocationDto locationDto = null;
        if (company.getLocation() != null) {
            locationDto = new LocationDto(company.getLocation().getId(), company.getLocation().getName());
        }
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsiteUrl(),
                locationDto,
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
