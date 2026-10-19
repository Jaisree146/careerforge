package com.careerforge.backend.modules.job.repository;

import com.careerforge.backend.modules.job.model.Job;
import com.careerforge.backend.modules.job.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    List<Job> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);

    Page<Job> findByStatusOrderByCreatedAtDesc(JobStatus status, Pageable pageable);
}
