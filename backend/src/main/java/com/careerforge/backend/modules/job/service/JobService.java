package com.careerforge.backend.modules.job.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.company.dto.CompanyResponse;
import com.careerforge.backend.modules.company.dto.LocationDto;
import com.careerforge.backend.modules.company.model.Company;
import com.careerforge.backend.modules.company.model.Location;
import com.careerforge.backend.modules.company.repository.CompanyRepository;
import com.careerforge.backend.modules.company.repository.LocationRepository;
import com.careerforge.backend.modules.job.dto.JobRequest;
import com.careerforge.backend.modules.job.dto.JobResponse;
import com.careerforge.backend.modules.job.dto.JobSkillDto;
import com.careerforge.backend.modules.job.model.EmploymentType;
import com.careerforge.backend.modules.job.model.Job;
import com.careerforge.backend.modules.job.model.JobStatus;
import com.careerforge.backend.modules.job.model.Skill;
import com.careerforge.backend.modules.job.repository.JobRepository;
import com.careerforge.backend.modules.job.repository.SkillRepository;
import com.careerforge.backend.modules.profile.model.RecruiterProfile;
import com.careerforge.backend.modules.profile.repository.RecruiterProfileRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final SkillRepository skillRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            LocationRepository locationRepository,
            SkillRepository skillRepository,
            RecruiterProfileRepository recruiterProfileRepository
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.locationRepository = locationRepository;
        this.skillRepository = skillRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    @Transactional
    public JobResponse createJob(Long recruiterId, JobRequest request) {
        User recruiter = validateAndGetRecruiter(recruiterId);

        Company company = resolveCompany(recruiterId, request.getCompanyId());

        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + request.getLocationId()));
        } else if (company.getLocation() != null) {
            location = company.getLocation();
        }

        Job job = new Job(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getExperience(),
                request.getSalary(),
                request.getStatus() != null ? request.getStatus() : JobStatus.DRAFT,
                request.getEmploymentType() != null ? request.getEmploymentType() : EmploymentType.FULL_TIME,
                request.getDeadline(),
                company,
                recruiter,
                location
        );

        attachSkills(job, request.getSkills());

        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long recruiterId, Long jobId, JobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        validateOwnership(job, recruiterId);

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setExperience(request.getExperience());
        job.setSalary(request.getSalary());
        job.setEmploymentType(request.getEmploymentType());
        job.setDeadline(request.getDeadline());

        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + request.getLocationId()));
            job.setLocation(location);
        }

        if (request.getCompanyId() != null && !request.getCompanyId().equals(job.getCompany().getId())) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));
            job.setCompany(company);
        }

        job.clearSkills();
        attachSkills(job, request.getSkills());

        Job updatedJob = jobRepository.save(job);
        return mapToResponse(updatedJob);
    }

    @Transactional
    public JobResponse updateJobStatus(Long recruiterId, Long jobId, JobStatus newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        validateOwnership(job, recruiterId);
        job.setStatus(newStatus);
        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getRecruiterJobs(Long recruiterId) {
        validateAndGetRecruiter(recruiterId);
        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchPublishedJobs(
            String title,
            Long locationId,
            Long companyId,
            EmploymentType employmentType,
            Pageable pageable
    ) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce PUBLISHED status for candidate search
            predicates.add(cb.equal(root.get("status"), JobStatus.PUBLISHED));

            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase().trim() + "%"));
            }

            if (locationId != null) {
                predicates.add(cb.equal(root.get("location").get("id"), locationId));
            }

            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    private void attachSkills(Job job, List<JobSkillDto> skillDtos) {
        if (skillDtos == null) return;
        for (JobSkillDto skillDto : skillDtos) {
            Skill skill = skillRepository.findById(skillDto.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillDto.getSkillId()));
            job.addSkill(skill, skillDto.isRequired());
        }
    }

    private Company resolveCompany(Long recruiterId, Long companyIdFromRequest) {
        if (companyIdFromRequest != null) {
            return companyRepository.findById(companyIdFromRequest)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyIdFromRequest));
        }

        RecruiterProfile profile = recruiterProfileRepository.findById(recruiterId)
                .orElseThrow(() -> new BadRequestException("Recruiter profile not found. Please link a company to your profile first."));

        if (profile.getCompany() == null) {
            throw new BadRequestException("Recruiter is not associated with any company. Please set your company first.");
        }

        return profile.getCompany();
    }

    private void validateOwnership(Job job, Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recruiterId));

        if (recruiter.getRole() == Role.ADMIN) {
            return; // Admins can manage any job
        }

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new BadRequestException("Unauthorized: You do not have permission to modify this job listing");
        }
    }

    private User validateAndGetRecruiter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.RECRUITER && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User does not have RECRUITER permissions");
        }

        return user;
    }

    private JobResponse mapToResponse(Job job) {
        Company c = job.getCompany();
        LocationDto compLocDto = null;
        if (c.getLocation() != null) {
            compLocDto = new LocationDto(c.getLocation().getId(), c.getLocation().getName());
        }
        CompanyResponse compResponse = new CompanyResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getWebsiteUrl(),
                compLocDto,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );

        LocationDto jobLocDto = null;
        if (job.getLocation() != null) {
            jobLocDto = new LocationDto(job.getLocation().getId(), job.getLocation().getName());
        }

        List<JobSkillDto> skills = job.getJobSkills().stream()
                .map(js -> new JobSkillDto(js.getSkill().getId(), js.getSkill().getName(), js.isRequired()))
                .collect(Collectors.toList());

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getExperience(),
                job.getSalary(),
                job.getStatus(),
                job.getEmploymentType(),
                job.getDeadline(),
                compResponse,
                job.getRecruiter().getId(),
                job.getRecruiter().getName(),
                jobLocDto,
                skills,
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
