package com.careerforge.backend.modules.matching.engine;

import com.careerforge.backend.modules.matching.dto.ExperienceMatchDetails;
import com.careerforge.backend.modules.matching.dto.MatchScoreBreakdown;
import com.careerforge.backend.modules.matching.dto.SkillMatchDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RuleBasedWeightedMatchingEngine implements MatchingEngine {

    private static final double WEIGHT_SKILLS = 0.60;
    private static final double WEIGHT_EXPERIENCE = 0.25;
    private static final double WEIGHT_PROFILE = 0.15;

    @Override
    public MatchScoreBreakdown calculateMatch(CandidateProfileData candidate, JobPostingData job) {
        Set<String> candidateSkills = candidate.getSkillNames().stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> reqSkills = job.getRequiredSkillNames().stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> optSkills = job.getOptionalSkillNames().stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> missingOptional = new ArrayList<>();

        for (String req : reqSkills) {
            if (candidateSkills.contains(req)) {
                matchedSkills.add(req);
            } else {
                missingRequired.add(req);
            }
        }

        for (String opt : optSkills) {
            if (candidateSkills.contains(opt)) {
                if (!matchedSkills.contains(opt)) {
                    matchedSkills.add(opt);
                }
            } else {
                missingOptional.add(opt);
            }
        }

        double skillScore;
        if (reqSkills.isEmpty() && optSkills.isEmpty()) {
            skillScore = 85.0;
        } else {
            double reqScore = reqSkills.isEmpty() ? 100.0 : ((double) (reqSkills.size() - missingRequired.size()) / reqSkills.size()) * 100.0;
            double optScore = optSkills.isEmpty() ? reqScore : ((double) (optSkills.size() - missingOptional.size()) / optSkills.size()) * 100.0;
            skillScore = (reqScore * 0.75) + (optScore * 0.25);
        }

        SkillMatchDetails skillDetails = new SkillMatchDetails(
                matchedSkills,
                missingRequired,
                missingOptional,
                round(skillScore)
        );

        double candExp = candidate.getExperienceYears();
        double reqExp = job.getRequiredExperienceYears();
        boolean meetsExp = candExp >= reqExp;
        double expScore;
        if (reqExp <= 0) {
            expScore = 100.0;
        } else if (candExp >= reqExp) {
            expScore = 100.0;
        } else {
            expScore = Math.min(100.0, (candExp / reqExp) * 100.0);
        }

        ExperienceMatchDetails expDetails = new ExperienceMatchDetails(
                round(candExp),
                round(reqExp),
                job.getRequiredExperienceText(),
                meetsExp,
                round(expScore)
        );

        double profileScore = 0.0;
        if (candidate.isHasResume()) profileScore += 40.0;
        if (candidate.isHasBio()) profileScore += 20.0;
        if (candidate.isHasEducation()) profileScore += 20.0;
        if (candExp > 0) profileScore += 20.0;

        double overallScore = (skillScore * WEIGHT_SKILLS) +
                              (expScore * WEIGHT_EXPERIENCE) +
                              (profileScore * WEIGHT_PROFILE);
        overallScore = round(overallScore);

        String fitLevel;
        if (overallScore >= 80.0) {
            fitLevel = "STRONG_MATCH";
        } else if (overallScore >= 60.0) {
            fitLevel = "GOOD_MATCH";
        } else if (overallScore >= 40.0) {
            fitLevel = "MODERATE_MATCH";
        } else {
            fitLevel = "LOW_MATCH";
        }

        List<String> reasons = new ArrayList<>();
        if (!reqSkills.isEmpty()) {
            int matchedReqCount = reqSkills.size() - missingRequired.size();
            reasons.add(String.format("Matches %d of %d required skills (%.0f%%)", matchedReqCount, reqSkills.size(), ((double) matchedReqCount / reqSkills.size()) * 100));
        }
        if (!missingRequired.isEmpty()) {
            reasons.add("Missing required: " + String.join(", ", missingRequired));
        }
        if (reqExp > 0) {
            if (meetsExp) {
                reasons.add(String.format("Meets required experience (%.1f yrs candidate vs %.1f yrs required)", candExp, reqExp));
            } else {
                reasons.add(String.format("Below required experience (%.1f yrs candidate vs %.1f yrs required)", candExp, reqExp));
            }
        }
        if (candidate.isHasResume()) {
            reasons.add("Profile complete with verified resume");
        } else {
            reasons.add("Upload a resume to boost match score");
        }

        return new MatchScoreBreakdown(
                overallScore,
                round(skillScore),
                round(expScore),
                round(profileScore),
                fitLevel,
                skillDetails,
                expDetails,
                reasons
        );
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
