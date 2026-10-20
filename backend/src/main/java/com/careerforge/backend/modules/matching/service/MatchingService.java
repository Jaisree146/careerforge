package com.careerforge.backend.modules.matching.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.model.CandidateSkill;
import com.careerforge.backend.modules.job.model.Job;
import com.careerforge.backend.modules.job.model.JobSkill;
import com.careerforge.backend.modules.job.model.JobStatus;
import com.careerforge.backend.modules.job.repository.CandidateSkillRepository;
import com.careerforge.backend.modules.job.repository.JobRepository;
import com.careerforge.backend.modules.job.repository.JobSkillRepository;
import com.careerforge.backend.modules.matching.dto.CandidateMatchResponse;
import com.careerforge.backend.modules.matching.dto.JobMatchResponse;
import com.careerforge.backend.modules.matching.dto.MatchScoreBreakdown;
import com.careerforge.backend.modules.matching.engine.CandidateProfileData;
import com.careerforge.backend.modules.matching.engine.JobPostingData;
import com.careerforge.backend.modules.matching.engine.MatchingEngine;
import com.careerforge.backend.modules.profile.model.CandidateProfile;
import com.careerforge.backend.modules.profile.model.Experience;
import com.careerforge.backend.modules.profile.model.Resume;
import com.careerforge.backend.modules.profile.repository.CandidateProfileRepository;
import com.careerforge.backend.modules.profile.repository.EducationRepository;
import com.careerforge.backend.modules.profile.repository.ExperienceRepository;
import com.careerforge.backend.modules.profile.repository.ResumeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final MatchingEngine matchingEngine;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final CandidateSkillRepository candidateSkillRepository;

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+)(?:\\+|-)?");

    public MatchingService(
            MatchingEngine matchingEngine,
            JobRepository jobRepository,
            JobSkillRepository jobSkillRepository,
            UserRepository userRepository,
            CandidateProfileRepository candidateProfileRepository,
            ResumeRepository resumeRepository,
            EducationRepository educationRepository,
            ExperienceRepository experienceRepository,
            CandidateSkillRepository candidateSkillRepository
    ) {
        this.matchingEngine = matchingEngine;
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.resumeRepository = resumeRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.candidateSkillRepository = candidateSkillRepository;
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponse> getJobMatchesForCandidate(Long candidateId, int limit) {
        CandidateProfileData candidateData = buildCandidateProfileData(candidateId);
        List<Job> publishedJobs = jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.PUBLISHED, Pageable.unpaged()).getContent();

        List<JobMatchResponse> matches = new ArrayList<>();
        for (Job job : publishedJobs) {
            JobPostingData jobData = buildJobPostingData(job);
            MatchScoreBreakdown matchBreakdown = matchingEngine.calculateMatch(candidateData, jobData);

            matches.add(new JobMatchResponse(
                    job.getId(),
                    job.getTitle(),
                    job.getCompany() != null ? job.getCompany().getName() : null,
                    job.getLocation() != null ? job.getLocation().getName() : null,
                    job.getSalary(),
                    job.getEmploymentType() != null ? job.getEmploymentType().name() : null,
                    matchBreakdown
            ));
        }

        return matches.stream()
                .sorted(Comparator.comparingDouble((JobMatchResponse m) -> m.getMatch().getOverallScore()).reversed())
                .limit(limit > 0 ? limit : 20)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobMatchResponse getJobMatchDetails(Long candidateId, Long jobId) {
        CandidateProfileData candidateData = buildCandidateProfileData(candidateId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        JobPostingData jobData = buildJobPostingData(job);
        MatchScoreBreakdown matchBreakdown = matchingEngine.calculateMatch(candidateData, jobData);

        return new JobMatchResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getLocation() != null ? job.getLocation().getName() : null,
                job.getSalary(),
                job.getEmploymentType() != null ? job.getEmploymentType().name() : null,
                matchBreakdown
        );
    }

    @Transactional(readOnly = true)
    public List<CandidateMatchResponse> getCandidateMatchesForJob(Long recruiterId, Long jobId, int limit) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (recruiter.getRole() != Role.ADMIN && !job.getRecruiter().getId().equals(recruiterId)) {
            throw new BadRequestException("Unauthorized: You can only view candidate matches for your own jobs");
        }

        JobPostingData jobData = buildJobPostingData(job);

        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CANDIDATE)
                .collect(Collectors.toList());

        List<CandidateMatchResponse> matches = new ArrayList<>();
        for (User candidate : candidates) {
            CandidateProfileData candidateData = buildCandidateProfileData(candidate.getId());
            MatchScoreBreakdown matchBreakdown = matchingEngine.calculateMatch(candidateData, jobData);

            Optional<Resume> resume = resumeRepository.findByUserId(candidate.getId());
            matches.add(new CandidateMatchResponse(
                    candidate.getId(),
                    candidate.getName(),
                    candidate.getEmail(),
                    resume.map(Resume::getFileUrl).orElse(null),
                    matchBreakdown
            ));
        }

        return matches.stream()
                .sorted(Comparator.comparingDouble((CandidateMatchResponse m) -> m.getMatch().getOverallScore()).reversed())
                .limit(limit > 0 ? limit : 20)
                .collect(Collectors.toList());
    }

    private CandidateProfileData buildCandidateProfileData(Long candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        Optional<CandidateProfile> profile = candidateProfileRepository.findById(candidateId);
        boolean hasBio = profile.isPresent() && profile.get().getBio() != null && !profile.get().getBio().isBlank();

        boolean hasResume = resumeRepository.findByUserId(candidateId).isPresent();
        boolean hasEducation = !educationRepository.findByUserIdOrderByStartDateDesc(candidateId).isEmpty();

        List<Experience> experiences = experienceRepository.findByUserIdOrderByStartDateDesc(candidateId);
        double experienceYears = calculateTotalExperienceYears(experiences);

        List<CandidateSkill> candidateSkills = candidateSkillRepository.findByUserIdWithSkill(candidateId);
        Set<String> skillNames = candidateSkills.stream()
                .map(cs -> cs.getSkill().getName())
                .collect(Collectors.toSet());

        return new CandidateProfileData(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                hasResume,
                hasBio,
                hasEducation,
                experienceYears,
                skillNames
        );
    }

    private JobPostingData buildJobPostingData(Job job) {
        List<JobSkill> jobSkills = jobSkillRepository.findByJobIdWithSkill(job.getId());

        Set<String> requiredSkills = new HashSet<>();
        Set<String> optionalSkills = new HashSet<>();

        for (JobSkill js : jobSkills) {
            if (js.isRequired()) {
                requiredSkills.add(js.getSkill().getName());
            } else {
                optionalSkills.add(js.getSkill().getName());
            }
        }

        double requiredYears = parseExperienceYears(job.getExperience());

        return new JobPostingData(
                job.getId(),
                job.getTitle(),
                job.getExperience(),
                requiredYears,
                requiredSkills,
                optionalSkills
        );
    }

    private double calculateTotalExperienceYears(List<Experience> experiences) {
        long totalMonths = 0;
        LocalDate now = LocalDate.now();

        for (Experience exp : experiences) {
            LocalDate start = exp.getStartDate();
            LocalDate end = exp.getEndDate() != null ? exp.getEndDate() : now;
            if (start != null && !start.isAfter(end)) {
                totalMonths += ChronoUnit.MONTHS.between(start, end);
            }
        }

        return (double) totalMonths / 12.0;
    }

    private double parseExperienceYears(String experienceText) {
        if (experienceText == null || experienceText.isBlank()) {
            return 0.0;
        }
        Matcher matcher = EXPERIENCE_PATTERN.matcher(experienceText.trim());
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }
}
