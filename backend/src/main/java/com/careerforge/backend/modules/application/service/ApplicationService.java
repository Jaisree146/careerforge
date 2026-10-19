package com.careerforge.backend.modules.application.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.application.dto.ApplicationApplyRequest;
import com.careerforge.backend.modules.application.dto.ApplicationResponse;
import com.careerforge.backend.modules.application.dto.ApplicationStatusUpdateRequest;
import com.careerforge.backend.modules.application.dto.SavedJobResponse;
import com.careerforge.backend.modules.application.model.Application;
import com.careerforge.backend.modules.application.model.ApplicationStatus;
import com.careerforge.backend.modules.application.model.SavedJob;
import com.careerforge.backend.modules.application.repository.ApplicationRepository;
import com.careerforge.backend.modules.application.repository.SavedJobRepository;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.model.Job;
import com.careerforge.backend.modules.job.model.JobStatus;
import com.careerforge.backend.modules.job.repository.JobRepository;
import com.careerforge.backend.modules.profile.model.Resume;
import com.careerforge.backend.modules.profile.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            SavedJobRepository savedJobRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            ResumeRepository resumeRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public ApplicationResponse applyToJob(Long candidateId, Long jobId, ApplicationApplyRequest request) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        if (candidate.getRole() != Role.CANDIDATE && candidate.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only candidates can submit job applications");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("Applications can only be submitted for PUBLISHED jobs. Current status: " + job.getStatus());
        }

        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("The application deadline for this job passed on: " + job.getDeadline());
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new BadRequestException("You have already submitted an application for this job");
        }

        Resume resume;
        if (request != null && request.getResumeId() != null) {
            resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + request.getResumeId()));
            if (!resume.getUserId().equals(candidateId)) {
                throw new BadRequestException("Unauthorized: Selected resume does not belong to your account");
            }
        } else {
            resume = resumeRepository.findByUserId(candidateId)
                    .orElseThrow(() -> new BadRequestException("No resume found in your profile. Please upload a resume before applying"));
        }

        String notes = request != null ? request.getNotes() : null;
        Application application = new Application(candidate, job, resume, ApplicationStatus.APPLIED, notes);
        Application saved = applicationRepository.save(application);

        return mapToApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getCandidateApplications(Long candidateId, ApplicationStatus status, Pageable pageable) {
        Page<Application> page;
        if (status != null) {
            page = applicationRepository.findByCandidateIdAndStatus(candidateId, status, pageable);
        } else {
            page = applicationRepository.findByCandidateId(candidateId, pageable);
        }
        return page.map(this::mapToApplicationResponse);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getCandidateApplicationById(Long candidateId, Long applicationId) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new BadRequestException("Unauthorized: Application belongs to another candidate");
        }

        return mapToApplicationResponse(application);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getJobApplicationsForRecruiter(Long recruiterId, Long jobId, ApplicationStatus status, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + jobId));

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recruiterId));

        if (recruiter.getRole() != Role.ADMIN && !job.getRecruiter().getId().equals(recruiterId)) {
            throw new BadRequestException("Unauthorized: You can only view applications for your own job postings");
        }

        Page<Application> page;
        if (status != null) {
            page = applicationRepository.findByJobIdAndStatus(jobId, status, pageable);
        } else {
            page = applicationRepository.findByJobId(jobId, pageable);
        }
        return page.map(this::mapToApplicationResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplicationsForRecruiter(Long recruiterId, ApplicationStatus status, Pageable pageable) {
        Page<Application> page;
        if (status != null) {
            page = applicationRepository.findByJobRecruiterIdAndStatus(recruiterId, status, pageable);
        } else {
            page = applicationRepository.findByJobRecruiterId(recruiterId, pageable);
        }
        return page.map(this::mapToApplicationResponse);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long recruiterId, Long applicationId, ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recruiterId));

        if (recruiter.getRole() != Role.ADMIN && !application.getJob().getRecruiter().getId().equals(recruiterId)) {
            throw new BadRequestException("Unauthorized: You can only update applications for your own job postings");
        }

        application.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            application.setNotes(request.getNotes().trim());
        }

        Application updated = applicationRepository.save(application);
        return mapToApplicationResponse(updated);
    }

    @Transactional
    public void saveJob(Long userId, Long jobId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!savedJobRepository.existsByIdUserIdAndIdJobId(userId, jobId)) {
            SavedJob savedJob = new SavedJob(userId, job);
            savedJobRepository.save(savedJob);
        }
    }

    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        if (!savedJobRepository.existsByIdUserIdAndIdJobId(userId, jobId)) {
            throw new ResourceNotFoundException("Job is not saved in bookmarks");
        }
        savedJobRepository.deleteByIdUserIdAndIdJobId(userId, jobId);
    }

    @Transactional(readOnly = true)
    public Page<SavedJobResponse> getSavedJobs(Long userId, Pageable pageable) {
        return savedJobRepository.findSavedJobsWithDetails(userId, pageable)
                .map(this::mapToSavedJobResponse);
    }

    @Transactional(readOnly = true)
    public boolean isJobSaved(Long userId, Long jobId) {
        return savedJobRepository.existsByIdUserIdAndIdJobId(userId, jobId);
    }

    private ApplicationResponse mapToApplicationResponse(Application a) {
        return new ApplicationResponse(
                a.getId(),
                a.getJob().getId(),
                a.getJob().getTitle(),
                a.getJob().getCompany() != null ? a.getJob().getCompany().getName() : null,
                a.getCandidate().getId(),
                a.getCandidate().getName(),
                a.getCandidate().getEmail(),
                a.getResume() != null ? a.getResume().getId() : null,
                a.getResume() != null ? a.getResume().getFileUrl() : null,
                a.getStatus(),
                a.getNotes(),
                a.getAppliedAt(),
                a.getUpdatedAt()
        );
    }

    private SavedJobResponse mapToSavedJobResponse(SavedJob sj) {
        Job job = sj.getJob();
        return new SavedJobResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getLocation() != null ? job.getLocation().getName() : null,
                job.getSalary(),
                job.getEmploymentType() != null ? job.getEmploymentType().name() : null,
                sj.getCreatedAt()
        );
    }
}
