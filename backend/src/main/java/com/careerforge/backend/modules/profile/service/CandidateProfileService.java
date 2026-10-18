package com.careerforge.backend.modules.profile.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.profile.dto.CandidateProfileRequest;
import com.careerforge.backend.modules.profile.dto.CandidateProfileResponse;
import com.careerforge.backend.modules.profile.dto.EducationDto;
import com.careerforge.backend.modules.profile.dto.ExperienceDto;
import com.careerforge.backend.modules.profile.dto.ResumeDto;
import com.careerforge.backend.modules.profile.model.CandidateProfile;
import com.careerforge.backend.modules.profile.model.Education;
import com.careerforge.backend.modules.profile.model.Experience;
import com.careerforge.backend.modules.profile.model.Resume;
import com.careerforge.backend.modules.profile.repository.CandidateProfileRepository;
import com.careerforge.backend.modules.profile.repository.EducationRepository;
import com.careerforge.backend.modules.profile.repository.ExperienceRepository;
import com.careerforge.backend.modules.profile.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateProfileService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository profileRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final ResumeRepository resumeRepository;

    public CandidateProfileService(
            UserRepository userRepository,
            CandidateProfileRepository profileRepository,
            EducationRepository educationRepository,
            ExperienceRepository experienceRepository,
            ResumeRepository resumeRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(Long userId) {
        User user = validateAndGetCandidateUser(userId);
        CandidateProfile profile = profileRepository.findById(userId).orElse(null);

        List<EducationDto> educations = educationRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(e -> new EducationDto(e.getId(), e.getDegree(), e.getInstitution(), e.getStartDate(), e.getEndDate()))
                .collect(Collectors.toList());

        List<ExperienceDto> experiences = experienceRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(e -> new ExperienceDto(e.getId(), e.getCompanyName(), e.getRole(), e.getStartDate(), e.getEndDate()))
                .collect(Collectors.toList());

        ResumeDto resumeDto = resumeRepository.findByUserId(userId)
                .map(r -> new ResumeDto(r.getId(), r.getFileUrl(), r.getUploadedAt()))
                .orElse(null);

        return new CandidateProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profile != null ? profile.getBio() : null,
                resumeDto,
                educations,
                experiences,
                profile != null ? profile.getCreatedAt() : user.getCreatedAt(),
                profile != null ? profile.getUpdatedAt() : user.getUpdatedAt()
        );
    }

    @Transactional
    public CandidateProfileResponse updateBio(Long userId, CandidateProfileRequest request) {
        validateAndGetCandidateUser(userId);

        CandidateProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> new CandidateProfile(userId, null));

        profile.setBio(request.getBio());
        profileRepository.save(profile);

        return getProfile(userId);
    }

    @Transactional
    public EducationDto addEducation(Long userId, EducationDto dto) {
        validateAndGetCandidateUser(userId);

        Education education = new Education(
                userId,
                dto.getDegree().trim(),
                dto.getInstitution().trim(),
                dto.getStartDate(),
                dto.getEndDate()
        );
        Education saved = educationRepository.save(education);
        return new EducationDto(saved.getId(), saved.getDegree(), saved.getInstitution(), saved.getStartDate(), saved.getEndDate());
    }

    @Transactional
    public void deleteEducation(Long userId, Long educationId) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education record not found with id: " + educationId));

        if (!education.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized: Education record belongs to another user");
        }

        educationRepository.delete(education);
    }

    @Transactional
    public ExperienceDto addExperience(Long userId, ExperienceDto dto) {
        validateAndGetCandidateUser(userId);

        Experience experience = new Experience(
                userId,
                dto.getCompanyName().trim(),
                dto.getRole().trim(),
                dto.getStartDate(),
                dto.getEndDate()
        );
        Experience saved = experienceRepository.save(experience);
        return new ExperienceDto(saved.getId(), saved.getCompanyName(), saved.getRole(), saved.getStartDate(), saved.getEndDate());
    }

    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience record not found with id: " + experienceId));

        if (!experience.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized: Experience record belongs to another user");
        }

        experienceRepository.delete(experience);
    }

    @Transactional
    public ResumeDto uploadOrUpdateResume(Long userId, ResumeDto dto) {
        validateAndGetCandidateUser(userId);

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseGet(() -> new Resume(userId, dto.getFileUrl().trim()));

        resume.setFileUrl(dto.getFileUrl().trim());
        Resume saved = resumeRepository.save(resume);

        return new ResumeDto(saved.getId(), saved.getFileUrl(), saved.getUploadedAt());
    }

    private User validateAndGetCandidateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.CANDIDATE && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User with ID " + userId + " does not have CANDIDATE role");
        }

        return user;
    }
}
