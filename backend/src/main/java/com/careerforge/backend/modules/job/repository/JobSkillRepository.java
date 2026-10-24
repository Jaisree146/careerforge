package com.careerforge.backend.modules.job.repository;

import com.careerforge.backend.modules.job.model.JobSkill;
import com.careerforge.backend.modules.job.model.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {

    List<JobSkill> findByIdJobId(Long jobId);

    @org.springframework.data.jpa.repository.Query("SELECT js FROM JobSkill js JOIN FETCH js.skill WHERE js.id.jobId = :jobId")
    List<JobSkill> findByJobIdWithSkill(@org.springframework.data.repository.query.Param("jobId") Long jobId);

    @org.springframework.data.jpa.repository.Query("SELECT js.skill.name, COUNT(js) FROM JobSkill js GROUP BY js.skill.name ORDER BY COUNT(js) DESC")
    List<Object[]> findTopDemandedSkills(org.springframework.data.domain.Pageable pageable);
}
