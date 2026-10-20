package com.careerforge.backend.modules.job.repository;

import com.careerforge.backend.modules.job.model.CandidateSkill;
import com.careerforge.backend.modules.job.model.CandidateSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, CandidateSkillId> {

    List<CandidateSkill> findByIdUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT cs FROM CandidateSkill cs JOIN FETCH cs.skill WHERE cs.id.userId = :userId")
    List<CandidateSkill> findByUserIdWithSkill(@org.springframework.data.repository.query.Param("userId") Long userId);

    boolean existsByIdUserIdAndIdSkillId(Long userId, Long skillId);

    void deleteByIdUserIdAndIdSkillId(Long userId, Long skillId);
}
