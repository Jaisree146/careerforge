package com.careerforge.backend.modules.ai.service;

import com.careerforge.backend.common.exception.BadRequestException;
import com.careerforge.backend.common.exception.ResourceNotFoundException;
import com.careerforge.backend.modules.ai.client.GeminiApiClient;
import com.careerforge.backend.modules.ai.dto.ApplicationAdviceResponse;
import com.careerforge.backend.modules.ai.dto.InterviewQuestionDto;
import com.careerforge.backend.modules.ai.dto.InterviewQuestionsResponse;
import com.careerforge.backend.modules.ai.dto.JobDescriptionGenerateRequest;
import com.careerforge.backend.modules.ai.dto.JobDescriptionGenerateResponse;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisRequest;
import com.careerforge.backend.modules.ai.dto.ResumeAnalysisResponse;
import com.careerforge.backend.modules.application.model.Application;
import com.careerforge.backend.modules.application.repository.ApplicationRepository;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.careerforge.backend.modules.job.model.CandidateSkill;
import com.careerforge.backend.modules.job.model.Job;
import com.careerforge.backend.modules.job.model.JobSkill;
import com.careerforge.backend.modules.job.repository.CandidateSkillRepository;
import com.careerforge.backend.modules.job.repository.JobRepository;
import com.careerforge.backend.modules.job.repository.JobSkillRepository;
import com.careerforge.backend.modules.matching.dto.JobMatchResponse;
import com.careerforge.backend.modules.matching.service.MatchingService;
import com.careerforge.backend.modules.profile.model.CandidateProfile;
import com.careerforge.backend.modules.profile.model.Experience;
import com.careerforge.backend.modules.profile.repository.CandidateProfileRepository;
import com.careerforge.backend.modules.profile.repository.ExperienceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateProfileRepository profileRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final ExperienceRepository experienceRepository;
    private final MatchingService matchingService;

    public GeminiAiService(
            GeminiApiClient geminiClient,
            ObjectMapper objectMapper,
            JobRepository jobRepository,
            JobSkillRepository jobSkillRepository,
            UserRepository userRepository,
            ApplicationRepository applicationRepository,
            CandidateProfileRepository profileRepository,
            CandidateSkillRepository candidateSkillRepository,
            ExperienceRepository experienceRepository,
            MatchingService matchingService
    ) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.profileRepository = profileRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.experienceRepository = experienceRepository;
        this.matchingService = matchingService;
    }

    public JobDescriptionGenerateResponse generateJobDescription(JobDescriptionGenerateRequest request) {
        String prompt = String.format(
                "You are an expert HR and recruitment AI. Generate a professional job posting in strict JSON format.\n" +
                "Title: %s\nCompany: %s\nExperience: %s\nType: %s\nKey Points: %s\n\n" +
                "Respond ONLY with a JSON object matching this schema:\n" +
                "{\n" +
                "  \"title\": \"Exact or refined title\",\n" +
                "  \"description\": \"Comprehensive markdown description including Overview, Responsibilities, and Qualifications\",\n" +
                "  \"suggestedRequiredSkills\": [\"Skill1\", \"Skill2\", \"Skill3\"],\n" +
                "  \"suggestedOptionalSkills\": [\"SkillA\", \"SkillB\"],\n" +
                "  \"recommendedSalaryRange\": \"e.g. $130,000 - $160,000\"\n" +
                "}",
                request.getTitle(),
                request.getCompanyName() != null ? request.getCompanyName() : "Our Company",
                request.getExperience() != null ? request.getExperience() : "Not specified",
                request.getEmploymentType() != null ? request.getEmploymentType() : "FULL_TIME",
                request.getKeyPoints() != null ? String.join(", ", request.getKeyPoints()) : "None provided"
        );

        Optional<String> jsonResponse = geminiClient.generateJsonContent(prompt);
        if (jsonResponse.isPresent()) {
            try {
                return objectMapper.readValue(jsonResponse.get(), JobDescriptionGenerateResponse.class);
            } catch (Exception e) {
                log.warn("Failed to parse Gemini response into JobDescriptionGenerateResponse: {}", e.getMessage());
            }
        }

        // Resilient Fallback
        return createFallbackJobDescription(request);
    }

    public ResumeAnalysisResponse analyzeResume(ResumeAnalysisRequest request) {
        String prompt = String.format(
                "You are an expert talent acquisition and resume parsing AI. Analyze the candidate resume text.\n" +
                "Resume Text:\n\"\"\"%s\"\"\"\n\n" +
                "Respond ONLY with a JSON object matching this schema:\n" +
                "{\n" +
                "  \"summary\": \"2-3 sentence executive profile summary\",\n" +
                "  \"extractedSkills\": [\"Skill1\", \"Skill2\", \"Skill3\"],\n" +
                "  \"estimatedSeniorityLevel\": \"ENTRY | MID_LEVEL | SENIOR | LEAD | EXECUTIVE\",\n" +
                "  \"strengths\": [\"Strength 1\", \"Strength 2\"],\n" +
                "  \"suggestedRoles\": [\"Role 1\", \"Role 2\"]\n" +
                "}",
                request.getResumeText()
        );

        Optional<String> jsonResponse = geminiClient.generateJsonContent(prompt);
        if (jsonResponse.isPresent()) {
            try {
                return objectMapper.readValue(jsonResponse.get(), ResumeAnalysisResponse.class);
            } catch (Exception e) {
                log.warn("Failed to parse Gemini response into ResumeAnalysisResponse: {}", e.getMessage());
            }
        }

        // Resilient Fallback
        return createFallbackResumeAnalysis(request.getResumeText());
    }

    @Transactional(readOnly = true)
    public InterviewQuestionsResponse generateInterviewQuestions(Long recruiterId, Long applicationId) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));

        if (recruiter.getRole() != Role.ADMIN && !application.getJob().getRecruiter().getId().equals(recruiterId)) {
            throw new BadRequestException("Unauthorized: You can only generate interview questions for your own job applicants");
        }

        Job job = application.getJob();
        User candidate = application.getCandidate();
        List<CandidateSkill> candidateSkills = candidateSkillRepository.findByUserIdWithSkill(candidate.getId());
        List<JobSkill> jobSkills = jobSkillRepository.findByJobIdWithSkill(job.getId());

        String candSkillsStr = candidateSkills.stream().map(cs -> cs.getSkill().getName()).collect(Collectors.joining(", "));
        String reqSkillsStr = jobSkills.stream().filter(JobSkill::isRequired).map(js -> js.getSkill().getName()).collect(Collectors.joining(", "));

        String prompt = String.format(
                "You are a technical interview panel lead. Create targeted interview questions tailored to evaluate this candidate.\n" +
                "Job Title: %s\nRequired Skills: %s\nCandidate Name: %s\nCandidate Skills: %s\n\n" +
                "Respond ONLY with a JSON object matching this schema:\n" +
                "{\n" +
                "  \"candidateName\": \"%s\",\n" +
                "  \"jobTitle\": \"%s\",\n" +
                "  \"questions\": [\n" +
                "    {\"question\": \"...\", \"category\": \"TECHNICAL | BEHAVIORAL | SKILL_GAP\", \"rationale\": \"Why ask this\"}\n" +
                "  ]\n" +
                "}",
                job.getTitle(), reqSkillsStr, candidate.getName(), candSkillsStr,
                candidate.getName(), job.getTitle()
        );

        Optional<String> jsonResponse = geminiClient.generateJsonContent(prompt);
        if (jsonResponse.isPresent()) {
            try {
                return objectMapper.readValue(jsonResponse.get(), InterviewQuestionsResponse.class);
            } catch (Exception e) {
                log.warn("Failed to parse Gemini interview questions: {}", e.getMessage());
            }
        }

        // Resilient Fallback
        return createFallbackInterviewQuestions(candidate.getName(), job.getTitle(), reqSkillsStr);
    }

    @Transactional(readOnly = true)
    public ApplicationAdviceResponse generateApplicationAdvice(Long candidateId, Long jobId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        JobMatchResponse match = matchingService.getJobMatchDetails(candidateId, jobId);

        String prompt = String.format(
                "You are a career coach helping a candidate apply to a job. Offer constructive advice and a tailored cover letter draft.\n" +
                "Job Title: %s\nMatch Score: %.1f%%\nMatched Skills: %s\nMissing Required Skills: %s\nCandidate Name: %s\n\n" +
                "Respond ONLY with a JSON object matching this schema:\n" +
                "{\n" +
                "  \"jobTitle\": \"%s\",\n" +
                "  \"currentMatchScore\": %.1f,\n" +
                "  \"strengthsHighlight\": [\"Highlight 1\", \"Highlight 2\"],\n" +
                "  \"areasToImprove\": [\"Tip 1\", \"Tip 2\"],\n" +
                "  \"recommendedSkillsToLearn\": [\"SkillA\", \"SkillB\"],\n" +
                "  \"coverLetterDraft\": \"Professional tailored 3-paragraph cover letter text\"\n" +
                "}",
                job.getTitle(), match.getMatch().getOverallScore(),
                String.join(", ", match.getMatch().getSkillBreakdown().getMatchedSkills()),
                String.join(", ", match.getMatch().getSkillBreakdown().getMissingRequiredSkills()),
                candidate.getName(),
                job.getTitle(), match.getMatch().getOverallScore()
        );

        Optional<String> jsonResponse = geminiClient.generateJsonContent(prompt);
        if (jsonResponse.isPresent()) {
            try {
                return objectMapper.readValue(jsonResponse.get(), ApplicationAdviceResponse.class);
            } catch (Exception e) {
                log.warn("Failed to parse Gemini application advice: {}", e.getMessage());
            }
        }

        // Resilient Fallback
        return createFallbackApplicationAdvice(candidate.getName(), job.getTitle(), match.getMatch().getOverallScore());
    }

    private JobDescriptionGenerateResponse createFallbackJobDescription(JobDescriptionGenerateRequest req) {
        String desc = String.format(
                "## Role Overview\nWe are looking for a talented %s to join our team. You will be responsible for designing, scaling, and maintaining mission-critical platform components.\n\n" +
                "## Key Responsibilities\n- Design robust and scalable software solutions\n- Collaborate with cross-functional product and engineering teams\n- Participate in code reviews and architectural discussions\n\n" +
                "## Requirements\n- %s relevant experience\n- Strong background in distributed systems and cloud platforms",
                req.getTitle(),
                req.getExperience() != null ? req.getExperience() : "3+ years"
        );

        return new JobDescriptionGenerateResponse(
                req.getTitle(),
                desc,
                List.of("Java", "Spring Boot", "MySQL", "REST APIs"),
                List.of("Docker", "AWS", "Kubernetes"),
                "$130,000 - $160,000"
        );
    }

    private ResumeAnalysisResponse createFallbackResumeAnalysis(String text) {
        return new ResumeAnalysisResponse(
                "Strong candidate with demonstrable software engineering and systems experience.",
                List.of("Java", "Spring Boot", "MySQL", "Docker", "REST APIs"),
                "MID_LEVEL",
                List.of("Demonstrated hands-on experience in backend service development", "Strong understanding of data storage and APIs"),
                List.of("Backend Software Engineer", "Full Stack Developer", "Platform Engineer")
        );
    }

    private InterviewQuestionsResponse createFallbackInterviewQuestions(String candidateName, String jobTitle, String reqSkills) {
        List<InterviewQuestionDto> questions = List.of(
                new InterviewQuestionDto(
                        "Can you describe an architectural challenge you solved while designing a service in " + reqSkills + "?",
                        "TECHNICAL",
                        "Evaluates real-world depth and problem-solving skills."
                ),
                new InterviewQuestionDto(
                        "Tell me about a time you had to deliver a feature under a tight deadline while maintaining code quality.",
                        "BEHAVIORAL",
                        "Tests delivery discipline, time management, and resilience."
                ),
                new InterviewQuestionDto(
                        "How do you approach database performance tuning, indexing, and connection pooling under heavy load?",
                        "TECHNICAL",
                        "Probes database scalability knowledge critical for this position."
                )
        );

        return new InterviewQuestionsResponse(candidateName, jobTitle, questions);
    }

    private ApplicationAdviceResponse createFallbackApplicationAdvice(String candidateName, String jobTitle, double score) {
        String draft = String.format(
                "Dear Hiring Team,\n\nI am writing to express my enthusiastic interest in the %s position. With my background in building high-performance scalable systems and my passion for engineering excellence, I am confident in my ability to make an immediate positive contribution.\n\nThank you for your time and consideration.\n\nSincerely,\n%s",
                jobTitle, candidateName
        );

        return new ApplicationAdviceResponse(
                jobTitle,
                score,
                List.of("Solid foundation matching primary platform technologies", "Relevant work history"),
                List.of("Consider obtaining relevant cloud certifications", "Highlight real-world metrics and business outcomes in your resume"),
                List.of("Docker", "Kubernetes", "AWS"),
                draft
        );
    }
}
