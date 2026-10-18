package com.careerforge.backend.modules.profile.repository;

import com.careerforge.backend.modules.profile.model.RecruiterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Long> {

    List<RecruiterProfile> findByCompanyId(Long companyId);
}
