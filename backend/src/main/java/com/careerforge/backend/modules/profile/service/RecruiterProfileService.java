package com.careerforge.backend.modules.profile.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.company.dto.CompanyResponse;
import com.careerforge.backend.modules.company.dto.LocationDto;
import com.careerforge.backend.modules.company.model.Company;
import com.careerforge.backend.modules.company.repository.CompanyRepository;
import com.careerforge.backend.modules.profile.dto.RecruiterProfileRequest;
import com.careerforge.backend.modules.profile.dto.RecruiterProfileResponse;
import com.careerforge.backend.modules.profile.model.RecruiterProfile;
import com.careerforge.backend.modules.profile.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruiterProfileService {

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;

    public RecruiterProfileService(
            UserRepository userRepository,
            RecruiterProfileRepository recruiterProfileRepository,
            CompanyRepository companyRepository
    ) {
        this.userRepository = userRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterProfileResponse getProfile(Long userId) {
        User user = validateAndGetRecruiterUser(userId);
        RecruiterProfile profile = recruiterProfileRepository.findById(userId).orElse(null);

        CompanyResponse companyResponse = null;
        if (profile != null && profile.getCompany() != null) {
            Company c = profile.getCompany();
            LocationDto locDto = null;
            if (c.getLocation() != null) {
                locDto = new LocationDto(c.getLocation().getId(), c.getLocation().getName());
            }
            companyResponse = new CompanyResponse(
                    c.getId(),
                    c.getName(),
                    c.getDescription(),
                    c.getWebsiteUrl(),
                    locDto,
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }

        return new RecruiterProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                companyResponse,
                profile != null ? profile.getCreatedAt() : user.getCreatedAt(),
                profile != null ? profile.getUpdatedAt() : user.getUpdatedAt()
        );
    }

    @Transactional
    public RecruiterProfileResponse updateProfile(Long userId, RecruiterProfileRequest request) {
        validateAndGetRecruiterUser(userId);

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        RecruiterProfile profile = recruiterProfileRepository.findById(userId)
                .orElseGet(() -> new RecruiterProfile(userId, company));

        profile.setCompany(company);
        recruiterProfileRepository.save(profile);

        return getProfile(userId);
    }

    private User validateAndGetRecruiterUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User with ID " + userId + " does not have RECRUITER role");
        }

        return user;
    }
}
