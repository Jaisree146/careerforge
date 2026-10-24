package com.careerforge.backend.modules.application.repository;

import com.careerforge.backend.modules.application.model.SavedJob;
import com.careerforge.backend.modules.application.model.SavedJobId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, SavedJobId> {

    boolean existsByIdUserIdAndIdJobId(Long userId, Long jobId);

    Optional<SavedJob> findByIdUserIdAndIdJobId(Long userId, Long jobId);

    void deleteByIdUserIdAndIdJobId(Long userId, Long jobId);

    long countByIdUserId(Long userId);

    @Query(
            value = "SELECT sj FROM SavedJob sj " +
                    "JOIN FETCH sj.job j " +
                    "JOIN FETCH j.company " +
                    "LEFT JOIN FETCH j.location " +
                    "WHERE sj.id.userId = :userId",
            countQuery = "SELECT COUNT(sj) FROM SavedJob sj WHERE sj.id.userId = :userId"
    )
    Page<SavedJob> findSavedJobsWithDetails(@Param("userId") Long userId, Pageable pageable);
}
