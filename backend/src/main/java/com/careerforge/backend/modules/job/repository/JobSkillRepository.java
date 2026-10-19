package com.careerforge.backend.modules.job.repository;

import com.careerforge.backend.modules.job.model.JobSkill;
import com.careerforge.backend.modules.job.model.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {

    List<JobSkill> findByIdJobId(Long jobId);
}
