package com.careerforge.backend.modules.matching.engine;

import com.careerforge.backend.modules.matching.dto.MatchScoreBreakdown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rule-Based Weighted Matching Engine Unit Tests")
class RuleBasedWeightedMatchingEngineTest {

    private RuleBasedWeightedMatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        matchingEngine = new RuleBasedWeightedMatchingEngine();
    }

    @Test
    @DisplayName("Should produce STRONG_MATCH (score >= 80) when candidate meets all skills and experience")
    void shouldProduceStrongMatchForIdealCandidate() {
        // Arrange
        CandidateProfileData candidate = new CandidateProfileData(
                1L,
                "Alex Rivera",
                "alex@example.com",
                true,  // hasResume
                true,  // hasBio
                true,  // hasEducation
                6.0,   // experienceYears
                Set.of("Java", "Spring Boot", "MySQL", "Docker", "AWS")
        );

        JobPostingData job = new JobPostingData(
                10L,
                "Senior Java Engineer",
                "5+ years required",
                5.0,
                Set.of("Java", "Spring Boot", "MySQL"),
                Set.of("Docker", "AWS")
        );

        // Act
        MatchScoreBreakdown result = matchingEngine.calculateMatch(candidate, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOverallScore()).isGreaterThanOrEqualTo(80.0);
        assertThat(result.getFitLevel()).isEqualTo("STRONG_MATCH");
        assertThat(result.getSkillScore()).isEqualTo(100.0);
        assertThat(result.getExperienceScore()).isEqualTo(100.0);
        assertThat(result.getProfileCompletenessScore()).isEqualTo(100.0);
        assertThat(result.getSkillBreakdown().getMissingRequiredSkills()).isEmpty();
        assertThat(result.getSkillBreakdown().getMissingOptionalSkills()).isEmpty();
        assertThat(result.getExperienceBreakdown().isMeetsRequirement()).isTrue();
    }

    @Test
    @DisplayName("Should compute partial skill score when candidate is missing some required skills")
    void shouldComputePartialSkillScoreWhenMissingRequired() {
        // Arrange
        CandidateProfileData candidate = new CandidateProfileData(
                2L,
                "Jordan Lee",
                "jordan@example.com",
                true,
                true,
                false,
                3.0,
                Set.of("Java", "MySQL")
        );

        JobPostingData job = new JobPostingData(
                11L,
                "Backend Engineer",
                "3+ years required",
                3.0,
                Set.of("Java", "Spring Boot", "MySQL", "Kubernetes"), // candidate has 2 of 4 (50%)
                Collections.emptySet()
        );

        // Act
        MatchScoreBreakdown result = matchingEngine.calculateMatch(candidate, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSkillBreakdown().getMatchedSkills()).containsExactlyInAnyOrder("java", "mysql");
        assertThat(result.getSkillBreakdown().getMissingRequiredSkills()).containsExactlyInAnyOrder("spring boot", "kubernetes");
        assertThat(result.getSkillScore()).isEqualTo(50.0);
        assertThat(result.getExperienceScore()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should penalize experience score proportionally when candidate experience is below required")
    void shouldPenalizeExperienceScoreWhenUnderExperienced() {
        // Arrange
        CandidateProfileData candidate = new CandidateProfileData(
                3L,
                "Junior Dev",
                "junior@example.com",
                true,
                false,
                true,
                2.0, // 2 years vs 5 years required -> 40%
                Set.of("Python", "Django")
        );

        JobPostingData job = new JobPostingData(
                12L,
                "Lead Python Architect",
                "5 years required",
                5.0,
                Set.of("Python", "Django"),
                Collections.emptySet()
        );

        // Act
        MatchScoreBreakdown result = matchingEngine.calculateMatch(candidate, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getExperienceBreakdown().isMeetsRequirement()).isFalse();
        assertThat(result.getExperienceScore()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should handle empty skills gracefully without throwing exceptions")
    void shouldHandleEmptySkillsGracefully() {
        // Arrange
        CandidateProfileData candidate = new CandidateProfileData(
                4L,
                "New Candidate",
                "new@example.com",
                false,
                false,
                false,
                0.0,
                Collections.emptySet()
        );

        JobPostingData job = new JobPostingData(
                13L,
                "Generalist Intern",
                "No experience needed",
                0.0,
                Collections.emptySet(),
                Collections.emptySet()
        );

        // Act
        MatchScoreBreakdown result = matchingEngine.calculateMatch(candidate, job);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSkillScore()).isEqualTo(85.0); // baseline when job specifies no skills
        assertThat(result.getExperienceScore()).isEqualTo(100.0); // 0 required exp -> 100%
        assertThat(result.getProfileCompletenessScore()).isEqualTo(0.0);
    }
}
