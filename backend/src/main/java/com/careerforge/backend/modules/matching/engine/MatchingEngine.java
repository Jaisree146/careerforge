package com.careerforge.backend.modules.matching.engine;

import com.careerforge.backend.modules.matching.dto.MatchScoreBreakdown;

public interface MatchingEngine {

    MatchScoreBreakdown calculateMatch(CandidateProfileData candidate, JobPostingData job);
}
