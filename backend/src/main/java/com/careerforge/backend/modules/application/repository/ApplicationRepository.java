package com.careerforge.backend.modules.application.repository;

import com.careerforge.backend.modules.application.model.Application;
import com.careerforge.backend.modules.application.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    Page<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    Page<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobRecruiterId(Long recruiterId, Pageable pageable);

    Page<Application> findByJobRecruiterIdAndStatus(Long recruiterId, ApplicationStatus status, Pageable pageable);

    long countByJobId(Long jobId);

    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);

    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.candidate " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company " +
           "JOIN FETCH a.resume " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);
}
