package com.careerforge.backend.modules.profile.repository;

import com.careerforge.backend.modules.profile.model.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
}
